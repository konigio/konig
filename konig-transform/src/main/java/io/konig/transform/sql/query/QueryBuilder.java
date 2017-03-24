package io.konig.transform.sql.query;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.Expression;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.ExistsExpression;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.NotExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.sql.query.UpdateItem;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.sql.query.WhereClause;
import io.konig.transform.IriTemplateElement;
import io.konig.transform.IriTemplateInfo;
import io.konig.transform.MappedId;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformAttribute;
import io.konig.transform.TransformFrame;

public class QueryBuilder {
	
	private String idColumnName = "id";
	private Graph graph;
	private OwlReasoner reasoner;
	
	
	public QueryBuilder(Graph graph) {
		this.graph = graph;
		reasoner = new OwlReasoner(graph);
	}
	
	public BigQueryCommandLine insertCommand(TransformFrame frame) throws ShapeTransformException {
		BigQueryCommandLine cmd = null;


		BigQueryTableReference tableRef = currentStateTableRef(frame);
		
		if (tableRef != null) {
			TableName targetTable = tableName(tableRef, null);
			List<ColumnExpression> columnList = columnList(frame);

			
			SelectExpression select = insertSelect(targetTable, frame);
			
			InsertStatement insert = new InsertStatement(targetTable.getExpression(), columnList, select);
			
			cmd = new BigQueryCommandLine();
			cmd.setProjectId(tableRef.getProjectId());
			cmd.setDml(insert);
			cmd.setUseLegacySql(false);
			
		}
		
		
		
		return cmd;
	}
	
	private SelectExpression insertSelect(TableName targetTable, TransformFrame frame) throws ShapeTransformException {

		SqlFrameFactory factory = new SqlFrameFactory();
		SqlFrame s = factory.create(frame);
		s.setAliasRequired(true);
		s.setTargetTableName(targetTable);
		SelectExpression select = selectExpression(s);
				
		addWhereClauseForInsert(s, select);
		
		return select;
	}

	private void addWhereClauseForInsert(SqlFrame s, SelectExpression select) throws ShapeTransformException {
		
		MappedId mappedId = s.getTransformFrame().getMappedId();
		TableName selfJoinTable = nextTable(s, s.getTargetTableName().getFullName());
		ColumnExpression leftColumn = null;
		ColumnExpression rightColumn = null;
		SelectExpression selectKey = new SelectExpression();
		if (mappedId != null) {
			IriTemplateInfo info = mappedId.getTemplateInfo();
			PropertyConstraint primaryKey = getPrimaryKey(info);
			if (primaryKey == null) {
				throw new ShapeTransformException("Primary key not found for Shape: " + 
						s.getTransformFrame().getTargetShape().getId());
			}
			
			
			
			
		
			String leftColumnName = null;
			if (primaryKey.getCompiledEquivalentPath() != null) {
				
				if (primaryKey.getCompiledEquivalentPath().asList().size()>1) {

					throw new ShapeTransformException("Cannot handle equivalentPath for primary key on Shape: " + 
							s.getTransformFrame().getTargetShape().getId());
				}
				
				SqlAttribute attr = sqlAttrForSourceProperty(s, primaryKey);
				leftColumnName = attr.getAttribute().getPredicate().getLocalName();
			} else {
				leftColumnName = primaryKey.getPredicate().getLocalName();
			}

			TableName sourceTable = s.getTableName(mappedId);
			leftColumn = selfJoinTable.column(leftColumnName);
			rightColumn = sourceTable.column(primaryKey);
			selectKey.add(new ColumnExpression(primaryKey.getPredicate().getLocalName()));
			
		} else {
			TableName sourceIdTable = sourceIdTable(s);
			if (sourceIdTable != null) {
				
				leftColumn = selfJoinTable.column("id");
				rightColumn = sourceIdTable.column("id");
				selectKey.add(new ColumnExpression("id"));
				
			} else {
				throw new ShapeTransformException("No identifier found for Shape "+ 
						s.getTransformFrame().getTargetShape().getId());
			}
		}

		ComparisonPredicate comparison = new ComparisonPredicate(ComparisonOperator.EQUALS, leftColumn, rightColumn);
		
		ExistsExpression exists = new ExistsExpression(selectKey);
		NotExpression notExists = new NotExpression(exists);

		selectKey.getFrom().add(selfJoinTable.getItem());
		selectKey.setWhere(new WhereClause(comparison));
		select.setWhere(new WhereClause(notExists));
	
		
	}


	private TableName sourceIdTable(SqlFrame s) {
		TableName result = null;
		Shape targetShape = s.getTransformFrame().getTargetShape();
		if (targetShape.getNodeKind() == NodeKind.IRI) {

			URI targetClass = targetShape.getTargetClass();
			for (JoinInfo join : s.getTableList()) {
				JoinElement right = join.getRight();
				Shape shape = right.getShapePath().getShape();
				if (targetClass.equals(shape.getTargetClass()) && shape.getNodeKind()==NodeKind.IRI) {
					result = right.getTableName();
					break;
				}
				
			}
		}
		return result;
	}

	private SqlAttribute sqlAttrForSourceProperty(SqlFrame s, PropertyConstraint p) {
		URI predicate = p.getPredicate();
		for (SqlAttribute a : s.getAttributes()) {
			if (a.getMappedProperty().getProperty().getPredicate().equals(predicate)) {
				return a;
			}
		}
		return null;
	}

	private TableName nextTable(SqlFrame s, String tableName) {
		char c = (char)('a' + s.getTableList().size());
		String alias = new String(new char[]{c});
		
		return new TableName(tableName, alias);
	}

	private PropertyConstraint getPrimaryKey(IriTemplateInfo info) throws ShapeTransformException {
		PropertyConstraint key = null;
		for (IriTemplateElement e : info) {
			PropertyConstraint p = e.getProperty();
			if (p != null) {
				if (key != null) {
					throw new ShapeTransformException("Dual keys are not supported");
				}
				key = p;
			}
		}
		return key;
	}

	private List<ColumnExpression> columnList(TransformFrame frame) {
		List<ColumnExpression> list = new ArrayList<>();
		if (frame.getTargetShape().getNodeKind()==NodeKind.IRI) {
			list.add(new ColumnExpression(idColumnName));
		}
		for (TransformAttribute attr : frame.getAttributes()) {
			String columnName = attr.getPredicate().getLocalName();
			list.add(new ColumnExpression(columnName));
		}
		return list;
	}

	private BigQueryTableReference currentStateTableRef(TransformFrame frame) {
		Shape shape = frame.getTargetShape();
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds.isA(Konig.GoogleBigQueryTable) && ds.isA(Konig.CurrentState)) {
				GoogleBigQueryTable bigQuery = (GoogleBigQueryTable) ds;
				return bigQuery.getTableReference();
			}
		}
		
		return null;
	}

	private TableName tableName(BigQueryTableReference tableRef, String alias) {

		StringBuilder builder = new StringBuilder();
		builder.append(tableRef.getDatasetId());
		builder.append('.');
		builder.append(tableRef.getTableId());
		String tableName = builder.toString();
		return new TableName(tableName, alias);
	}
	

	public BigQueryCommandLine updateCommand(TransformFrame frame) throws ShapeTransformException {
		

		SqlFrameFactory factory = new SqlFrameFactory(1);
		SqlFrame s = factory.create(frame);
		s.setAliasRequired(true);

		GoogleBigQueryTable bqTargetTable = bqTargetTable(s.getTransformFrame());
		s.setTargetTableName(tableName(bqTargetTable.getTableReference(), "a"));
		
		UpdateExpression update = updateExpression(s);

		BigQueryCommandLine cmd = new BigQueryCommandLine();
		String projectId = bqTargetTable.getTableReference().getProjectId();
		cmd.setProjectId(projectId);
		cmd.setDml(update);
		cmd.setUseLegacySql(false);
		return cmd;
	}

	private GoogleBigQueryTable bqTargetTable(TransformFrame transformFrame) {
		GoogleBigQueryTable result = null;
		Shape shape = transformFrame.getTargetShape();
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds instanceof GoogleBigQueryTable) {
				result = (GoogleBigQueryTable) ds;
				break;
			}
		}
		return result;
	}

	private UpdateExpression updateExpression(SqlFrame s) throws ShapeTransformException {
		
		UpdateExpression update = new UpdateExpression();
		update.setTable(s.getTableItem());
		

		TableName targetTable = s.getTargetTableName();
		for (SqlAttribute attr : s.getAttributes()) {
			ColumnExpression left = targetTable.column(attr.getAttribute().getTargetProperty());
			QueryExpression right = valueExpression(attr);
			UpdateItem item = new UpdateItem(left, right);
			update.add(item);
		}
		
		
		FromExpression from = new FromExpression();
		from.add(fromExpression(s));
		
		update.setFrom(from);
		
		// Set the where clause
		Shape targetShape = s.getTransformFrame().getTargetShape();
		if (targetShape.getNodeKind() == NodeKind.IRI) {
			
			MappedId mappedId = s.getTransformFrame().getMappedId();
			if (mappedId != null) {

				IriTemplateInfo templateInfo = mappedId.getTemplateInfo();
				if (templateInfo != null) {
					TableName tableName = s.getTableName(mappedId);

					FunctionExpression right = idValue(tableName, templateInfo);
					
					ColumnExpression left = targetTable.column("id");
					ComparisonPredicate comparison = new ComparisonPredicate(ComparisonOperator.EQUALS, left, right);
					update.setWhere(comparison);
				}
			}
			
		} else {
			throw new ShapeTransformException("Expected Shape to have IRI node kind: " + targetShape.getId());
		}
		
		return update;
	}


	public BigQueryCommandLine bigQueryCommandLine(TransformFrame frame) throws ShapeTransformException {
		
		String destinationTable = bigQueryTableId(frame);
		
		if (destinationTable == null) {
			return null;
		}

		TableRef tableId= new TableRef(destinationTable);
		SelectExpression select = selectExpression(frame);
		BigQueryCommandLine cmd = new BigQueryCommandLine();
		
		cmd.setProjectId(tableId.projectName);
		cmd.setDestinationTable(tableId.datasetTable());
		cmd.setUseLegacySql(false);
		cmd.setDml(select);
		
		return cmd;
	}
	
	private String bigQueryTableId(TransformFrame frame) {
		
		List<DataSource> list = frame.getTargetShape().getShapeDataSource();
		if (list != null) {
			for (DataSource source : list) {
				if (source instanceof TableDataSource) {
					TableDataSource table = (TableDataSource) source;
					return table.getTableIdentifier();
				}
			}
		}
		
		return null;
	}

	private SelectExpression selectExpression(SqlFrame s) throws ShapeTransformException {
		SelectExpression select = new SelectExpression();
		
		addFrom(select, s);
		
		addIdAttribute(s, select);
		
		for (SqlAttribute attr : s.getAttributes()) {
			select.add(valueExpression(attr));
		}
		
		return select;
	}
	public SelectExpression selectExpression(TransformFrame frame) throws ShapeTransformException {

		SqlFrameFactory factory = new SqlFrameFactory();
		SqlFrame s = factory.create(frame);
		
		return selectExpression(s);
		
	}
	

	private ValueExpression valueExpression(SqlAttribute attr) throws ShapeTransformException {
		
		TransformAttribute attribute = attr.getAttribute();
		SqlFrame embedded = attr.getEmbedded();
		MappedProperty mappedProperty = attr.getMappedProperty();
		TableName sourceTable = attr.getSourceTable();
		
		String sourceName = null;
		ValueExpression result = attr.getValueExpression();
		String targetName = attribute.getPredicate().getLocalName();
		PropertyConstraint p = mappedProperty==null ? null : mappedProperty.getProperty();
		
		if (result != null) {
			result = new AliasExpression(result, targetName);
			
		} else if (p!=null && p.getFormula()!=null) {
			SqlFormulaFactory factory = new SqlFormulaFactory();
			result = factory.formula(sourceTable, p);
			result = new AliasExpression(result, targetName);
			
		} else if (embedded == null) {
			if (mappedProperty.isLeaf()) {
				

				sourceName = p.getPredicate().getLocalName();
				
				result = (p.getFormula()==null) ? 
					sourceTable.column(sourceName) :
					formula(p.getFormula());
					
				if (!targetName.equals(sourceName)) {
					result = new AliasExpression(result, targetName);
				}
				
			} else if (mappedProperty.getTemplateInfo() != null) {
				FunctionExpression func = QueryBuilder.idValue(null, mappedProperty.getTemplateInfo());
				result = new AliasExpression(func, targetName);
			} else {
				
				result = enumValue(attr, targetName, false);
				if (result == null) {
					result = iriRef(attr, targetName);
				}
				if (result == null) {
					result = incompletePath(attr, targetName);
				}
			}
			
		} else {
			StructExpression struct = new StructExpression();
			for (SqlAttribute a : embedded.getAttributes()) {
				struct.add(valueExpression(a));
			}
			result = new AliasExpression(struct, targetName);
		}
		if (result == null) {
			throw new ShapeTransformException("Undefined value: " + attr.getAttribute().getPredicate().stringValue());
		}

		return result;
	}

	/**
	 * Compute the ValueExpression in the case where the SqlAttribute
	 * is defined by a MappedProperty that corresponds to a portion of 
	 * the equivalentPath.
	 * @param attr
	 * @param targetName
	 * @return
	 * @throws ShapeTransformException 
	 */
	private ValueExpression incompletePath(SqlAttribute attr, String targetName) throws ShapeTransformException {
		
		MappedProperty m = attr.getMappedProperty();
		if (m != null) {
			int stepIndex = m.getStepIndex();
			if (stepIndex > 0) {
				Path path = m.getProperty().getCompiledEquivalentPath();
				if (stepIndex < path.length()-1) {
					PropertyConstraint targetConstraint = attr.getAttribute().getTargetProperty();
					Resource valueClass = targetConstraint.getValueClass();
					if (valueClass != null) {
						if (reasoner.isSubClassOf(valueClass, Schema.Enumeration)) {
							return enumValue(attr, targetName, true);
						}
					}
					
					throw new ShapeTransformException("Unsupported expression: " + targetConstraint.getPredicate().stringValue());
				}
			}
		}
		return null;
	}

	private ColumnExpression formula(Expression formula) {
		// TODO Auto-generated method stub
		return null;
	}

	private ValueExpression iriRef(SqlAttribute attr, String targetName) {
		
		MappedProperty m = attr.getMappedProperty();
		IriTemplateInfo template = m.getTemplateInfo();
		ShapePath shape = m.getTemplateShape();
		if (template != null && shape!=null) {
			// TODO
			System.out.println("TODO: fixme");
			throw new KonigException("Unsupported operation");
		}
		return null;
	}

	private void addIdAttribute(SqlFrame s, SelectExpression select) throws ShapeTransformException {
		TransformFrame frame = s.getTransformFrame();
		MappedId mappedId = frame.getMappedId();
		if (mappedId != null) {
			
			IriTemplateInfo templateInfo = mappedId.getTemplateInfo();
			if (templateInfo != null) {
				TableName tableName = s.getTableName(mappedId);
				addIriReference(tableName, select, templateInfo, idColumnName);
			}
		} else {
			Shape shape = frame.getTargetShape();
			if (shape.getNodeKind() == NodeKind.IRI || shape.getNodeKind()==NodeKind.BlankNodeOrIRI) {
				
				List<JoinInfo> tableList = s.getTableList();
				if (!tableList.isEmpty()) {
					JoinInfo join = tableList.get(0);
					Shape rightShape = join.getRight().getShapePath().getShape();
					if (rightShape.getNodeKind()==NodeKind.IRI || shape.getNodeKind()==NodeKind.BlankNodeOrIRI) {
						TableName rightTable = join.getRight().getTableName();
						select.add(rightTable.column("id"));
					}
				}
				
			}
		}
		
	}

	
	
	private TableItemExpression fromExpression(SqlFrame s) throws ShapeTransformException {
		TableItemExpression left = null;
		List<JoinInfo> list = s.getTableList();
		boolean useTableAlias = s.isAliasRequired();
		for (JoinInfo joinInfo: list) {
			
			
			TableName rightTableName = joinInfo.getRight().getTableName();
			if (!useTableAlias) {
				rightTableName.setAlias(null);
			}
			
			TableItemExpression rightItem = rightTableName.getItem();
			
			if (left == null) {
				left = rightItem;
			} else {
				ValueExpression leftValue = joinInfo.getLeft().valueExpression();
				ValueExpression rightValue = joinInfo.getRight().valueExpression();

				OnExpression on = new OnExpression(
					new ComparisonPredicate(
						ComparisonOperator.EQUALS,
						leftValue,
						rightValue
					)
				);
				left = new JoinExpression(left, rightItem, on);
				
			}
			
//			
//			
//			TableName rightTableName = joinInfo.getRightTable();
//			if (!useTableAlias) {
//				rightTableName.setAlias(null);
//			}
//			
//			if (left == null) {
//				left = rightTableName.getItem();
//			} else {
//				MappedProperty leftProperty = joinInfo.getLeftProperty();
//				TableName leftTableName = joinInfo.getLeftTable();
//				ShapePath rightShapePath = joinInfo.getRightShapePath();
//				
//				
//				
//				ValueExpression leftColumn = null;
//
//				Shape rightShape = rightShapePath.getShape();
//				
//				ValueExpression leftValue = leftColumn;
//				ValueExpression rightValue = null;
//				
//				if (leftProperty != null) {
//					leftColumn = leftTableName.column(leftProperty.getProperty());
//				} else {
//					Shape leftShape = joinInfo.getLeftShapePath().getShape();
//					if (leftShape.getNodeKind() == NodeKind.IRI) {
//						leftColumn = leftTableName.column("id");
//					} else if (leftShape.getIriTemplate()!=null) {
//						IriTemplateInfo templateInfo = new IriTemplateInfo(leftShape.getIriTemplate());
//						leftColumn = idValue(leftTableName, templateInfo);
//					} else {
//						throw new ShapeTransformException("Shape must have nodeKind=IRI or supply an IRITemplate: " 
//								+ leftShape.getId().stringValue());
//					}
//				}
//				
//				
//				if (rightShape.getNodeKind() == NodeKind.IRI) {
//					rightValue = rightTableName.column("id");
//				} else {
//					MappedProperty rightProperty = joinInfo.getRightProperty();
//					IriTemplateInfo template = rightProperty.getTemplateInfo();
//					if (template == null) {
//						IriTemplate t = rightShape.getIriTemplate();
//						if (t != null) {
//							template = IriTemplateInfo.create(t, null, rightShape);
//						} else {
//							throw new ShapeTransformException(
//									"Expected Shape to have nodeKind=IRI or define an IRI template: " +
//									rightShape.getId().stringValue());
//						}
//					}
//					
//					rightValue = idValue(rightTableName, template);
//				}
//				
//				TableItemExpression rightItem = rightTableName.getItem();
//				OnExpression on = new OnExpression(
//					new ComparisonPredicate(
//						ComparisonOperator.EQUALS,
//						leftValue,
//						rightValue
//					)
//				);
//				left = new JoinExpression(left, rightItem, on);
//			}
		}
		if (left == null) {
			throw new ShapeTransformException("No tables found");
		}
		return left;
	}
	private void addFrom(SelectExpression select, SqlFrame s) throws ShapeTransformException {
		TableItemExpression left = fromExpression(s);
		select.getFrom().add(left);
		
	}



	
	private ValueExpression enumValue(SqlAttribute attr, String aliasName, boolean force) {
		MappedProperty m = attr.getMappedProperty();
		PropertyConstraint p = m.getProperty();
		if (p != null) {
			Path path = p.getCompiledEquivalentPath();
			if (path != null && (path.length()==2 || force)) {
				PropertyConstraint q = attr.getAttribute().getTargetProperty();
				if (q != null) {
					Resource targetClass = q.getValueClass();
					if (targetClass != null && (force || reasoner.isSubClassOf(targetClass, Schema.Enumeration))) {
						Step step = path.asList().get(path.length()-1);
						
						if (step instanceof OutStep) {

							OutStep out = (OutStep) step;
							URI predicate = out.getPredicate();
							Vertex classVertex = graph.getVertex(targetClass);
							
							
							if (classVertex != null) {
								List<Vertex> valueList = classVertex.asTraversal().in(RDF.TYPE).toVertexList();
								List<SimpleWhenClause> whenClauseList = new ArrayList<>();
								for (Vertex enumValue : valueList) {
									Resource enumValueId = enumValue.getId();
									if (enumValueId instanceof URI) {
										String localName = ((URI) enumValueId).getLocalName();
										Value value = enumValue.getValue(predicate);
										if (value != null) {
											StringLiteralExpression whenOperand = new StringLiteralExpression(value.stringValue());
											StringLiteralExpression result = new StringLiteralExpression(localName);
											SimpleWhenClause whenClause = new SimpleWhenClause(whenOperand, result);
											whenClauseList.add(whenClause);
										}
									}
								}
								
								TableName sourceTable = attr.getSourceTable();
								ColumnExpression caseOperand = sourceTable.column(p);
								
								SimpleCase simpleCase = new SimpleCase(caseOperand, whenClauseList, null);
								AliasExpression alias = new AliasExpression(simpleCase, aliasName);
								return alias;
							}
						}
						
						
					}
				}
			}
		}
		return null;
	}

	static FunctionExpression idValue(TableName tableName, IriTemplateInfo templateInfo) {
		StringBuffer buffer = null;
		FunctionExpression func = new FunctionExpression("CONCAT");
		for (IriTemplateElement e : templateInfo) {
			PropertyConstraint p = e.getProperty();
			Namespace ns = e.getNamespace();
			
			if (ns != null) {
				if (buffer == null) {
					buffer = new StringBuffer();
				}
				buffer.append(ns.getName());
			} else if (p != null) {
				
				if (buffer != null) {
					func.addArg(new StringLiteralExpression(buffer.toString()));
					buffer = null;
				}
				
				String columnName = columnName(tableName, p);
				func.addArg(new ColumnExpression(columnName));
			} else {
				if (buffer == null) {
					buffer = new StringBuffer();
				}
				buffer.append(e.getText());
			}
		}

		if (buffer != null) {
			func.addArg(new StringLiteralExpression(buffer.toString()));
		}
		return func;
	}

	private void addIriReference(TableName tableName, ValueContainer container, IriTemplateInfo templateInfo, String aliasName) {
		
		FunctionExpression func = idValue(tableName, templateInfo);
		container.add(new AliasExpression(func, aliasName));
		
	}
	

	static String columnName(TableName tableName, PropertyConstraint p) {
		URI predicate = p.getPredicate();
		if (tableName != null && tableName.getAlias()!=null && predicate!=null) {
			StringBuilder builder = new StringBuilder();
			builder.append(tableName.getAlias());
			builder.append('.');
			builder.append(predicate.getLocalName());
			return builder.toString();
		}
		return predicate==null ? null : predicate.getLocalName();
	}
	


	private static class TableRef {
		private String projectName;
		private String datasetName;
		private String tableName;
		
		public TableRef(String value) {
			String[] array = value.split("[.]");
			if (array.length==3) {
				projectName = array[0];
				datasetName = array[1];
				tableName = array[2];
			} else if (array.length==2) {
				projectName = "${projectId}";
				datasetName = array[0];
				tableName = array[1];
			}
		}
		
		public String datasetTable() {
			StringBuilder builder = new StringBuilder();
			if (datasetName != null) {
				builder.append(datasetName);
				builder.append('.');
			}
			builder.append(tableName);
			return builder.toString();
		}
	}
	
	
	

}
