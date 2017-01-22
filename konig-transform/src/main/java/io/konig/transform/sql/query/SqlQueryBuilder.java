package io.konig.transform.sql.query;

import java.util.List;

import org.openrdf.model.Namespace;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormatVisitor;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.PropertyTransform;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.ShapeTransformModel;
import io.konig.transform.TransformElement;

public class SqlQueryBuilder {
	
	private NamespaceManager nsManager;
		
	public SqlQueryBuilder(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}
	

	public BigQueryCommandLine toBigQueryCommandLine(ShapeTransformModel model) 
	throws ShapeTransformException {
		
		BigQueryCommandLine result = new BigQueryCommandLine();
		Shape targetShape = model.getTargetShape();
		
		String bqTableId = targetShape.getBigQueryTableId();
		
		if (bqTableId == null) {
			return null;
		}
		TableRef tableRef = new TableRef(bqTableId);
		
		result.setProjectId(tableRef.projectName);
		result.setDestinationTable(tableRef.datasetTable());
		result.setUseLegacySql(false);
		
		SelectExpression select = new SelectExpression();
		result.setSelect(select);
		
		TransformElement e = bestElement(model);
		
		
		if (e!= null) {
			Shape sourceShape = e.getSourceShape();
			String sourceTableId = sourceShape.getBigQueryTableId();
			
			if (sourceTableId != null) {
				
				addIdColumn(select, model, e);
				
				TableRef sourceTableRef = new TableRef(sourceTableId);
				TableItemExpression  x = select.fromTable(
						sourceTableRef.datasetName, sourceTableRef.tableName);
				
				if (x == null) {
					select.addFrom(sourceTableRef.datasetName, sourceTableRef.tableName);
				}
				
				for (PropertyTransform p : e.getPropertyTransformList()) {
					addProperty(select, p);
				}
			}
		}
		
		
		
		
		return result;
	}
	
	
	private void addIdColumn(ValueContainer container, ShapeTransformModel model, TransformElement e) {

		Shape targetShape = model.getTargetShape();
		NodeKind nodeKind = targetShape.getNodeKind();
		if (nodeKind == NodeKind.IRI || nodeKind==NodeKind.BlankNodeOrIRI) {

			Shape sourceShape = e.getSourceShape();
			
			IriTemplate iriTemplate = sourceShape.getIriTemplate();
			if (iriTemplate != null) {
				FunctionExpression func = new FunctionExpression("CONCAT");
				IriTemplateVisitor visitor = new IriTemplateVisitor(func);
				iriTemplate.traverse(visitor);
				
				container.add(new AliasExpression(func, "id"));
			}
		}
		
	}
	
	private class IriTemplateVisitor implements ValueFormatVisitor {
		FunctionExpression func;
		
		

		public IriTemplateVisitor(FunctionExpression func) {
			this.func = func;
		}

		@Override
		public void visitText(String text) {
			func.addArg(new StringLiteralExpression(text));
		}

		@Override
		public void visitVariable(String varName) {
			Namespace ns = nsManager.findByPrefix(varName);
			if (ns != null) {
				func.addArg(new StringLiteralExpression(ns.getName()));
			} else {
				func.addArg(new ColumnExpression(varName));
			}
		}
		
	}


	/**
	 * Choose the TransformElement that contains the most property transforms.
	 * 
	 * This is a bit of a hack which is necessary because do not yet support joins.
	 * 
	 * TODO: Replace this method and support joins.
	 * 
	 * @param model
	 */
	private TransformElement bestElement(ShapeTransformModel model) {
		TransformElement best = null;
		int max = 0;
		for (TransformElement e : model.getElements()) {
			int size = e.getPropertyTransformList().size();
			if (size > max) {
				max = size;
				best = e;
			}
		}
		
		return best;
	}



	private void addProperty(SelectExpression select, PropertyTransform p) throws ShapeTransformException {
			

		ValueContainer container = getOrCreateContainer(select, p);

		String aliasName = targetName(p.getTargetPath());
		

		if (container.getValue(aliasName) == null) {
			
			String columnName = columnName(p);
			
			ColumnExpression column = new ColumnExpression(columnName);
			
			if (columnName.equals(aliasName)) {
				container.add(column);
			} else {
				container.add(new AliasExpression(column, aliasName));
			}
		}
		
	}



	private ValueContainer getOrCreateContainer(SelectExpression select, PropertyTransform p) throws ShapeTransformException {
		ValueContainer result = select;
		List<Step> path = p.getTargetPath().asList();
		
		for (int i=0; i<path.size()-1; i++) {
			Step s = path.get(i);
			String targetName = targetName(s);
			ValueExpression value = result.getValue(targetName);
			if (value instanceof ValueContainer) {
				result = (ValueContainer) value;
			} else if (value == null) {
				StructExpression struct = new StructExpression();
			    result.add(new AliasExpression(struct, targetName));
				result = struct;
				
			} else if (value instanceof AliasExpression) {
				AliasExpression alias = (AliasExpression) value;
				if (alias.getExpression() instanceof ValueContainer) {
					result = (ValueContainer) alias.getExpression();
				} else {
					Path subpath = p.getTargetPath().subpath(0, i+1);
					throw new ShapeTransformException("Expected ValueContainer for path: "+ subpath.toString());
				}
				
			} else {
				Path subpath = p.getTargetPath().subpath(0, i+1);
				throw new ShapeTransformException("Expected ValueContainer for path: "+ subpath.toString());
			}
		}
		return result;
	}


	private String targetName(Step s) throws ShapeTransformException {
		if (s instanceof OutStep) {
			OutStep out = (OutStep) s;
			return out.getPredicate().getLocalName();
		}
		throw new ShapeTransformException("Step type not supported: " + s.getClass().getSimpleName());
	}


	private String targetName(Path path) throws ShapeTransformException {
		List<Step> list = path.asList();
		if (list.isEmpty()) {
			throw new ShapeTransformException("Empty path");
		}
		Step last = list.get(list.size()-1);
		
		return targetName(last);
	}


	private String columnName(PropertyTransform p) throws ShapeTransformException {
		List<Step> path = p.getSourcePath().asList();
		StringBuilder builder = new StringBuilder();
		String dot = "";
		for (Step s : path) {
			if (s instanceof OutStep) {
				OutStep out = (OutStep) s;
				builder.append(dot);
				builder.append(out.getPredicate().getLocalName());
			} else {
				throw new ShapeTransformException("Step type not supported: " + s.getClass().getSimpleName());
			}
			dot = ".";
		}
		return builder.toString();
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
