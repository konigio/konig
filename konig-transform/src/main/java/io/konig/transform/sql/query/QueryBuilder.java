package io.konig.transform.sql.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.IriTemplateElement;
import io.konig.transform.IriTemplateInfo;
import io.konig.transform.MappedId;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformAttribute;
import io.konig.transform.TransformFrame;

public class QueryBuilder {
	
	private String idColumnName = "id";
	
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

	public SelectExpression selectExpression(TransformFrame frame) {

		Namer namer = new Namer(frame);
		SelectExpression select = new SelectExpression();

		addFrom(namer, frame, select);
		
		addIdAttribute(frame, select);
		
		for (TransformAttribute attr : frame.getAttributes()) {
			addAttribute(namer, frame, select, attr);
		}
		
		return select;
	}

	private void addIdAttribute(TransformFrame frame, SelectExpression select) {
		
		MappedId mappedId = frame.getMappedId();
		if (mappedId != null) {
			
			IriTemplateInfo templateInfo = mappedId.getTemplateInfo();
			if (templateInfo != null) {
				addIriReference(select, templateInfo, idColumnName);
			}
		}
		
	}

	private FromExpression addFrom(Namer namer, TransformFrame frame, SelectExpression select) {
		
		
		FromExpression from = select.getFrom();
		
		TableItemExpression tableItem = buildFromExpression(namer, frame, null, null);
		from.add(tableItem);
		
		return from;
	}

	

	private TableItemExpression buildFromExpression(Namer namer, TransformFrame frame, TableItemExpression left, MappedProperty leftM) {

		
		for (TransformAttribute attr : frame.getAttributes()) {
			MappedProperty m = attr.getMappedProperty();
			if (m != null) {
				
				Shape sourceShape = m.getSourceShape();
				TableName tableName = namer.getTableName(sourceShape);
				
				if (tableName == null) {
					continue;
				}

				TransformFrame child = attr.getEmbeddedFrame();
				TableItemExpression right = tableName.getItem();
				
				if (right == null) {
					right = new TableNameExpression(tableName.getFullName());
					if (tableName.getAlias() != null) {
						right = new TableAliasExpression(right, tableName.getAlias());
					}
					tableName.setItem(right);
				}
				
				if (left != null && leftM != null  && left!=right) {
					OnExpression joinSpecification = joinSpecification(namer, left, leftM, right, m);
					left = new JoinExpression(left, right, joinSpecification);
				} else {
					left = right;
				}
				
				if (child != null) {
					left = buildFromExpression(namer, child, left, m);
				} 
			}
		}
		
		return left;
		
	}

	
	private OnExpression joinSpecification(
		Namer namer, 
		TableItemExpression a, 
		MappedProperty ap, 
		TableItemExpression b, 
		MappedProperty bp
	) {
		
		Shape aShape = ap.getSourceShape();
		TableName aTable = namer.getTableName(aShape);
		String leftColumn = columnName(aTable, ap.getProperty());
		
		Shape bShape = bp.getSourceShape();
		TableName bTable = namer.getTableName(bShape);
		String rightColumn = columnName(bTable, "id");
		
		return new OnExpression(
			new ComparisonPredicate(
				ComparisonOperator.EQUALS, 
				new ColumnExpression(leftColumn), 
				new ColumnExpression(rightColumn)
			)
		);
	}

	private void addAttribute(Namer namer, TransformFrame frame, ValueContainer container, TransformAttribute attr) {
		
		MappedProperty m = attr.getMappedProperty();
		if (m == null) {
			return;
		}
		
		Shape sourceShape = m.getSourceShape();
		TableName tableName = namer.getTableName(sourceShape);
//		if (tableName == null) {
//			throw new KonigException("TableName not found for shape <" + sourceShape.getId() + ">");
//		}

		PropertyConstraint targetProperty = attr.getTargetProperty();
		PropertyConstraint sourceProperty = m.getProperty();
		
		TransformFrame childFrame = attr.getEmbeddedFrame();
		String aliasName = columnName(null, targetProperty);
		if (childFrame == null) {
			
			
			if (m.isLeaf()) {
				String sourceColumn = columnName(tableName, sourceProperty);
				
				ValueExpression expr = new ColumnExpression(sourceColumn);
				
				if (requiresAlias(sourceColumn, aliasName)) {
					expr = new AliasExpression(expr, aliasName);
				}
				container.add(expr);
			} else if (m.getTemplateInfo()!=null) {
				addIriReference(container, m.getTemplateInfo(), aliasName);
			}
			
			
			 
		} else {
			
			StructExpression struct = new StructExpression();
			
			for (TransformAttribute a : childFrame.getAttributes()) {
				addAttribute(namer, childFrame, struct, a);
			}
			container.add(new AliasExpression(struct, aliasName));
		}
		
	}

	private boolean requiresAlias(String sourceColumn, String aliasName) {
		int start = sourceColumn.lastIndexOf('.') + 1;
		
		if (start == 0) {
			return !sourceColumn.equals(aliasName);
		}
		
		int len = sourceColumn.length()-start;
		if (len != aliasName.length()) {
			return true;
		}
		
		for (int i=0; i<len; i++) {
			if (sourceColumn.charAt(start+i) != aliasName.charAt(i)) {
				return true;
			}
		}
		
		return false;
	}

	private void addIriReference(ValueContainer container, IriTemplateInfo templateInfo, String aliasName) {
		
		
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
				
				String columnName = columnName(null, p);
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
		
		container.add(new AliasExpression(func, aliasName));
		
	}
	

	private String columnName(TableName tableName, String localName) {
		if (tableName != null && tableName.getAlias()!=null) {
			StringBuilder builder = new StringBuilder();
			builder.append(tableName.getAlias());
			builder.append('.');
			builder.append(localName);
			return builder.toString();
		}
		return localName;
	}

	private String columnName(TableName tableName, PropertyConstraint p) {
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
		
		public int size() {
			return tableNames.size();
		}
		
		public TableName getTableName(Shape shape) {
			return tableNames.get(shape);
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
	
	static private class TableName {
		private String fullName;
		private String alias;
		private TableItemExpression item;
		
		public TableName(String fullName, String alias) {
			this.fullName = fullName;
			this.alias = alias;
		}

		public String getFullName() {
			return fullName;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public TableItemExpression getItem() {
			return item;
		}

		public void setItem(TableItemExpression item) {
			this.item = item;
		}
		
	}

}
