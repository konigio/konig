package io.konig.transform.sql.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.Expression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.sql.query.UpdateItem;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
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

	public BigQueryCommandLine updateCommand(TransformFrame frame) throws ShapeTransformException {
		
		BigQueryCommandLine cmd = null;

		String destinationTable = bigQueryTableId(frame);
		if (destinationTable != null) {
			Namer namer = new Namer(frame);
			TableRef tableId = new TableRef(destinationTable);
			TableName tableName = namer.produceTargetTableName(tableId.datasetTable());
			UpdateExpression update = updateExpression(namer, frame);
			if (update != null) {
				
				update.setTable(tableName.getItem());
				cmd = new BigQueryCommandLine();
				cmd.setProjectId(tableId.projectName);
				cmd.setUseLegacySql(false);
				cmd.setSelect(update);
			}
		}
		return cmd;
	}

	private UpdateExpression updateExpression(Namer namer, TransformFrame frame) throws ShapeTransformException {
		UpdateExpression update = new UpdateExpression();
		addFrom(namer, frame, update, "");
		if (update.getFrom() == null) {
			return null;
		}
		
		// TODO: add update items
		// TODO: add where clause
		
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
		cmd.setSelect(select);
		
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

	
	public SelectExpression selectExpression(TransformFrame frame) throws ShapeTransformException {

		SqlFrameFactory factory = new SqlFrameFactory();
		SqlFrame s = factory.create(frame);
		
		SelectExpression select = new SelectExpression();
		
		addFrom(select, s);
		
		addIdAttribute(s, select);
		
		for (SqlAttribute attr : s.getAttributes()) {
			select.add(valueExpression(attr));
		}
		
		return select;
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

	private void addIdAttribute(SqlFrame s, SelectExpression select) {
		TransformFrame frame = s.getTransformFrame();
		MappedId mappedId = frame.getMappedId();
		if (mappedId != null) {
			
			IriTemplateInfo templateInfo = mappedId.getTemplateInfo();
			if (templateInfo != null) {
				addIriReference(select, templateInfo, idColumnName);
			}
		} else {
			Shape shape = frame.getTargetShape();
			if (shape.getNodeKind() == NodeKind.IRI || shape.getNodeKind()==NodeKind.BlankNodeOrIRI) {
				
				List<JoinInfo> tableList = s.getTableList();
				if (!tableList.isEmpty()) {
					JoinInfo join = tableList.get(0);
					Shape rightShape = join.getRightShapePath().getShape();
					if (rightShape.getNodeKind()==NodeKind.IRI || shape.getNodeKind()==NodeKind.BlankNodeOrIRI) {
						TableName rightTable = join.getRightTable();
						select.add(rightTable.column("id"));
					}
				}
				
			}
		}
		
	}

	
	

	private boolean addFrom(Namer namer, TransformFrame frame, UpdateExpression update, String path) throws ShapeTransformException {
		
		boolean result = false;
		FromExpression from = new FromExpression();
		update.setFrom(from);
	
		TableName targetTable = namer.getTargetTableName();
		
		BooleanTerm where = null;
		
		for (TransformAttribute attr : frame.getAttributes()) {
			MappedProperty m = attr.getMappedProperty();
			if (m != null) {
				Shape sourceShape = m.getSourceShape();
				TableName tableName = namer.producePathTableName(path, sourceShape);
				
				if (tableName == null) {
					continue;
				}
				TableItemExpression tableItem = tableName.getItem();
				if (tableItem == null) {
					tableItem = new TableNameExpression(tableName.getFullName());
					if (tableName.getAlias() != null) {
						tableItem = new TableAliasExpression(tableItem, tableName.getAlias());
					}
					tableName.setItem(tableItem);
				}
				if (!from.contains(tableItem)) {
					from.add(tableItem);
					
					BooleanTerm updateJoin = updateJoin(frame, m, namer.getTargetTableName(), tableName);
					if (where == null) {
						where = updateJoin;
					} else if (where instanceof AndExpression) {
						AndExpression and = (AndExpression) where;
						and.add(updateJoin);
					} else {
						AndExpression and = new AndExpression();
						and.add(where);
						and.add(updateJoin);
					}
				}
				PropertyConstraint aProperty = attr.getTargetProperty();
				if (aProperty != null) {

					Integer aMaxCount = aProperty.getMaxCount();

						
					if (aMaxCount != null && aMaxCount==1) {

						ColumnExpression left = targetTable.column(aProperty.getPredicate().getLocalName());
						ColumnExpression right = tableName.column(m.getProperty().getPredicate().getLocalName());
						
						UpdateItem item = new UpdateItem(left, right);
						update.add(item);
					} 
				}
				
			}
		}
		if (where != null) {
			update.setWhere(where);
		} 
		
		return result;
		
	}
	
	private BooleanTerm updateJoin(TransformFrame frame, MappedProperty m, TableName targetTable, TableName sourceTable) 
	throws ShapeTransformException {
		Shape sourceShape = m.getSourceShape();
		NodeKind kind = frame.getTargetShape().getNodeKind();
		if (kind != NodeKind.IRI) {
			throw new ShapeTransformException("Update method requires IRI nodeKind for shape " + frame.getTargetShape().getId());
		}
		ValueExpression left = targetTable.column("id");
		NodeKind sourceKind = sourceShape.getNodeKind();
		ValueExpression right = (sourceKind==NodeKind.IRI) ? sourceTable.column("id") : idValue(frame, m, sourceTable);
		if (right == null) {
			throw new ShapeTransformException("Source shape must have nodeKind=IRI or an iriTemplate: " + sourceShape.getId());
		}
		
		return new ComparisonPredicate(ComparisonOperator.EQUALS, left, right);
		
	}


	private ValueExpression idValue(TransformFrame frame, MappedProperty m, TableName sourceTable) {
		
		MappedId mappedId = frame.getIdMapping(m.getSourceShape());
		if (mappedId != null) {

			IriTemplateInfo template = mappedId.getTemplateInfo();
			if (template != null) {
				return idValue(sourceTable, template);
			}
		}
		
		return null;
	}

	private void addFrom(SelectExpression select, SqlFrame s) throws ShapeTransformException {
		TableItemExpression left = null;
		List<JoinInfo> list = s.getTableList();
		boolean useTableAlias = list.size()>1;
		for (JoinInfo joinInfo: list) {
			
			
			TableName rightTableName = joinInfo.getRightTable();
			if (!useTableAlias) {
				rightTableName.setAlias(null);
			}
			
			if (left == null) {
				left = rightTableName.getItem();
			} else {
				MappedProperty leftProperty = joinInfo.getLeftProperty();
				TableName leftTableName = joinInfo.getLeftTable();
				ShapePath rightShapePath = joinInfo.getRightShapePath();
				
				
				
				ValueExpression leftColumn = null;
				
				if (leftProperty != null) {
					leftColumn = leftTableName.column(leftProperty.getProperty());
				} else {
					Shape leftShape = joinInfo.getLeftShape();
					if (leftShape.getNodeKind() == NodeKind.IRI) {
						leftColumn = leftTableName.column("id");
					} else if (leftShape.getIriTemplate()!=null) {
						IriTemplateInfo templateInfo = new IriTemplateInfo(leftShape.getIriTemplate());
						leftColumn = idValue(leftTableName, templateInfo);
					} else {
						throw new ShapeTransformException("Shape must have nodeKind=IRI or supply an IRITemplate: " 
								+ leftShape.getId().stringValue());
					}
				}
				Shape rightShape = rightShapePath.getShape();
				
				ValueExpression leftValue = leftColumn;
				ValueExpression rightValue = null;
				
				
				if (rightShape.getNodeKind() == NodeKind.IRI) {
					rightValue = rightTableName.column("id");
				} else {
					MappedProperty rightProperty = joinInfo.getRightProperty();
					if (rightProperty == null || rightProperty.getTemplateInfo()==null) {
						throw new ShapeTransformException(
							"Expected Shape to have nodeKind=IRI or define an IRI template: " +
							rightShape.getId().stringValue());
					}
					
					rightValue = idValue(rightTableName, rightProperty.getTemplateInfo());
				}
				
				TableItemExpression rightItem = rightTableName.getItem();
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
//				MappedProperty m =j.getSourceProperty();
//				Shape aShape = frame.getTargetShape();
//
//				Shape aShape = ap.getSourceShape();
//				TableName aTable = namer.getTableName(aShape);
//				String leftColumn = columnName(aTable, ap.getProperty());
//				
//				Shape bShape = bp.getSourceShape();
//				TableName bTable = namer.getTableName(bShape);
//				String rightColumn = columnName(bTable, "id");
//				
//				return new OnExpression(
//					new ComparisonPredicate(
//						ComparisonOperator.EQUALS, 
//						new ColumnExpression(leftColumn), 
//						new ColumnExpression(rightColumn)
//					)
//				);
//				left = new JoinExpression(left, right, joinSpecification);
//			}
		}
		if (left == null) {
			throw new ShapeTransformException("No tables found");
		}
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

	private void addIriReference(ValueContainer container, IriTemplateInfo templateInfo, String aliasName) {
		
		FunctionExpression func = idValue(null, templateInfo);
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
	
	static private class Namer {
		private Map<Shape,TableName> tableNames = new HashMap<>();
		private Map<String, TableName> pathTable = new HashMap<>();
		private TableName targetTableName;
		
		public Namer(TransformFrame frame) {
			analyze(frame);
			commit();
		}

		private void commit() {
			if (tableNames.size()==1) {
				TableName n = tableNames.values().iterator().next();
				n.setAlias(null);
			}
		}
		
		public TableName produceTargetTableName(String tableRef) {
			String alias = nextAlias();
			targetTableName = new TableName(tableRef, alias);
			pathTable.put("_", targetTableName);
			
			return targetTableName;
		}
		
		public TableName getTargetTableName() {
			return targetTableName;
		}
		
		private String nextAlias() {
			char[] bytes = new char[1];
			bytes[0] = (char)('a' + pathTable.size());
			return new String(bytes);
		}

		public TableName producePathTableName(String path, Shape shape) {
			TableName name = pathTable.get(path);
			if (name == null) {
				TableName origin = produceTableName(shape);
				String alias = nextAlias();
				
				name = new TableName(origin.getFullName(), alias);
				pathTable.put(path, name);
			}
			
			return name;
			
		}
		
		private TableName produceTableName(Shape shape) {
			TableName n = tableNames.get(shape);
			
			if (n == null) {
				n = createTableName(shape);
			}
			
			return n;
		}
		
		private void analyze(TransformFrame frame) {

			for (TransformAttribute attr : frame.getAttributes()) {
				MappedProperty m = attr.getMappedProperty();
				if (m != null) {
					Shape sourceShape = m.getSourceShape();
					produceTableName(sourceShape);
				}
				TransformFrame child = attr.getEmbeddedFrame();
				if (child != null) {
					analyze(child);
				}
			}
			
		}
		
		
		private TableName createTableName(Shape shape) {
			List<DataSource> list = shape.getShapeDataSource();
			String fullName = null;
			if (list != null) {
				for (DataSource source : list) {
					if (source instanceof TableDataSource) {
						TableDataSource table = (TableDataSource) source;
						if (fullName == null) {
							fullName = table.getTableIdentifier();
						} else {
							StringBuilder err = new StringBuilder();
							err.append("Table name is ambiguous for shape <");
							err.append(shape.getId().stringValue());
							err.append(">.  Found '");
							err.append(fullName);
							err.append("' and '");
							err.append(table.getTableIdentifier());
							err.append("'");
							throw new KonigException(err.toString());
						}
					}
				}
			}
			if (fullName == null) {
				return null;
//				StringBuilder err = new StringBuilder();
//				err.append("No TableDataSource found for shape <");
//				err.append(shape.getId());
//				err.append('>');
//				throw new KonigException(err.toString());
			}
			
			String alias = alias(fullName);
			TableName n = new TableName(fullName, alias);
			tableNames.put(shape, n);
			return n;
		}

		public String alias(String tableFullName) {
			int index = tableFullName.lastIndexOf('.')+1;
			
			char c = Character.toLowerCase(tableFullName.charAt(index));
			
			int count = countAliasStart(c);
			
			StringBuilder builder = new StringBuilder();
			builder.append(c);
			if (count > 0) {
				builder.append(count+1);
			}
			
			return builder.toString();
		}

		private int countAliasStart(char c) {
			int count = 0;
			for (TableName n : tableNames.values()) {
				String alias = n.getAlias();
				char b = Character.toLowerCase(alias.charAt(0));
				if (c == b) {
					count++;
					if (alias.length()==1) {
						n.setAlias(alias + 1);
					}
				}
			}
			return count;
		}
	}
	
	

}
