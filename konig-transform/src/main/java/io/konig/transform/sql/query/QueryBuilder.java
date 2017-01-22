package io.konig.transform.sql.query;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.datasource.DataSource;
import io.konig.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
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
		List<DataSource> list = frame.getTargetShape().getShapeOf();
		if (list != null) {
			for (DataSource d : list) {
				if (d instanceof GoogleBigQueryTable) {
					return d.getIdentifier();
				}
			}
		}
		return null;
	}

	public SelectExpression selectExpression(TransformFrame frame) {
		
		SelectExpression select = new SelectExpression();
		
		addFrom(frame, select);
		
		addIdAttribute(frame, select);
		
		for (TransformAttribute attr : frame.getAttributes()) {
			addAttribute(frame, select, attr);
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

	private FromExpression addFrom(TransformFrame frame, SelectExpression select) {
		
		Set<String> set = new LinkedHashSet<>();
		collectFromTables(frame, set);
		
		FromExpression from = new FromExpression();
		for (String tableId : set) {
			String[] array = tableId.split("[.]");
			int size = array.length;
			
			String datasetName = size>1 ? array[size-2] : null;
			String tableName = size>0 ? array[size-1] : null;
			
			select.addFrom(datasetName, tableName);
		}
		
		return from;
	}

	

	private void collectFromTables(TransformFrame frame, Set<String> set) {
		
		for (TransformAttribute attr : frame.getAttributes()) {
			MappedProperty m = attr.getMappedProperty();
			if (m != null) {
				Shape sourceShape = m.getSourceShape();
				String tableId = sourceShape.getBigQueryTableId();
				if (tableId != null) {
					set.add(tableId);
				}
				TransformFrame child = attr.getEmbeddedFrame();
				if (child != null) {
					collectFromTables(child, set);
				}
			}
		}
		
	}

	private void addAttribute(TransformFrame frame, ValueContainer container, TransformAttribute attr) {
		
		MappedProperty m = attr.getMappedProperty();
		if (m == null) {
			return;
		}

		PropertyConstraint targetProperty = attr.getTargetProperty();
		PropertyConstraint sourceProperty = m.getProperty();
		
		TransformFrame childFrame = attr.getEmbeddedFrame();
		String aliasName = columnName(targetProperty);
		if (childFrame == null) {
			
			
			if (m.isLeaf()) {
				String sourceColumn = columnName(sourceProperty);
				
				ValueExpression expr = new ColumnExpression(sourceColumn);
				
				if (!aliasName.equals(sourceColumn)) {
					expr = new AliasExpression(expr, aliasName);
				}
				container.add(expr);
			} else if (m.getTemplateInfo()!=null) {
				addIriReference(container, m.getTemplateInfo(), aliasName);
			}
			
			
			 
		} else {
			
			StructExpression struct = new StructExpression();
			
			for (TransformAttribute a : childFrame.getAttributes()) {
				addAttribute(childFrame, struct, a);
			}
			container.add(new AliasExpression(struct, aliasName));
		}
		
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
				
				String columnName = columnName(p);
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

	private String columnName(PropertyConstraint p) {
		URI predicate = p.getPredicate();
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
