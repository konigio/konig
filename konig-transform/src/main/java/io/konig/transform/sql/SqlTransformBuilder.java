package io.konig.transform.sql;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.aws.datasource.AwsAuroraTable;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.FunctionExpression;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.Result;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.sql.query.UpdateItem;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.model.OutTPropertyShape;
import io.konig.transform.model.ShapeTransformException;
import io.konig.transform.model.SimpleTPropertyShape;
import io.konig.transform.model.TDataSource;
import io.konig.transform.model.TExpression;
import io.konig.transform.model.TFunctionExpression;
import io.konig.transform.model.TIriTemplateExpression;
import io.konig.transform.model.TIriTemplateItem;
import io.konig.transform.model.TLiteralExpression;
import io.konig.transform.model.TNodeShape;
import io.konig.transform.model.TPropertyShape;
import io.konig.transform.model.TReferenceDataExpression;
import io.konig.transform.model.TStructExpression;
import io.konig.transform.model.ValueOfExpression;

public class SqlTransformBuilder {
	private static final Logger logger = LoggerFactory.getLogger(SqlTransformBuilder.class);

	public void build(SqlTransform transform) throws ShapeTransformException {
		Worker worker = new Worker();
		worker.build(transform);
		
		
	}
	
	private static class Worker {
		private SqlTransform transform;
		private Set<TDataSource> dataSources = new HashSet<>();
		
		private TNodeShape currentNode;
		private TPropertyShape currentProperty;
		
		public void build(SqlTransform transform) throws ShapeTransformException {
			this.transform = transform;
			SelectExpression select = new SelectExpression();
			TableNameExpression tableName = targetTableName(transform.getTargetShape());
			List<ColumnExpression> columns = new ArrayList<>();
			InsertStatement insert = new InsertStatement(tableName, columns, select);
			UpdateExpression update = null;
			if (isMySQL()) {
				update = new UpdateExpression();
				insert.setUpdate(update);
			}
			transform.setInsert(insert);
			
			addValues(transform.getTargetShape(), select, columns, update);
			
			if (update != null && update.getItemList().isEmpty()) {
				insert.setUpdate(null);
			}
			
		}
		
		private String errorMessage(String message) {
			if (currentProperty != null) {
				StringBuilder builder = new StringBuilder();
				builder.append("At ");
				builder.append(currentProperty.getPath());
				builder.append(", ");
				builder.append(message);
				return builder.toString();
			}
			if (currentNode != null) {
				StringBuilder builder = new StringBuilder();
				builder.append("At ");
				builder.append(currentNode.getPath());
				builder.append(", ");
				builder.append(message);
				return builder.toString();
			}
			return message;
		}
		
		private void fail(String message) throws ShapeTransformException {
			throw new ShapeTransformException(errorMessage(message));
		}
		
		private boolean isMySQL() {
			DataSource ds = transform.getTargetShape().getTdatasource().getDatasource();
			return ds instanceof GoogleCloudSqlTable || ds instanceof AwsAuroraTable;
		}

		private TableNameExpression targetTableName(TNodeShape targetShape) throws ShapeTransformException {
			
			DataSource ds = targetShape.getTdatasource().getDatasource();
			
			if (ds instanceof TableDataSource) {
				TableDataSource tds = (TableDataSource) ds;
				return new TableNameExpression(tds.getQualifiedTableName());
			}
			
			String shapeId = RdfUtil.localName(targetShape.getShape().getId());
			throw new ShapeTransformException("In shape <" + shapeId + ">, expected TableDataSource but found "  + ds.getClass().getSimpleName());
		}

		private void addValues(TNodeShape targetShape, ValueContainer select, List<ColumnExpression> columns, UpdateExpression update) throws ShapeTransformException {
			
			currentNode = targetShape;
			for (TPropertyShape p : targetShape.getProperties()) {
				currentProperty = p;
				
				TExpression ve = p.getPropertyGroup().getValueExpression();
				if (ve instanceof TStructExpression) {
					AliasExpression alias = buildStruct(p, update);
					select.add(alias);
					if (columns != null) {
						columns.add(column(p));
					}
				} else {
					ValueExpression e = valueExpression(p, columns, update);
					select.add(e);
				}
				
			}
			currentProperty = null;
			currentNode = null;
			
		}



		private AliasExpression buildStruct(TPropertyShape p, UpdateExpression update) throws ShapeTransformException {

			TStructExpression e = (TStructExpression) p.getPropertyGroup().getValueExpression();
			TNodeShape targetValueShape = p.getValueShape();
			
			StructExpression struct = new StructExpression();
			addValues(targetValueShape, struct, null, update);
			String aliasName = p.getPredicate().getLocalName();
		
			return new AliasExpression(struct, aliasName);
		}

		private ValueExpression valueExpression(TPropertyShape p, List<ColumnExpression> columns, UpdateExpression update) throws ShapeTransformException {
			URI targetPredicate = p.getPredicate();
			ColumnExpression targetColumn = columns!=null || update!=null ?
					new ColumnExpression(targetPredicate.getLocalName()) : null;
					
			if (columns != null) {
				columns.add(targetColumn);
			}
			QueryExpression right = null;
			ValueExpression result = null;
			
			TExpression e = p.getPropertyGroup().getValueExpression();
			if (e instanceof ValueOfExpression) {
				ValueOfExpression valueOf = (ValueOfExpression) e;
				URI sourcePredicate = valueOf.getTpropertyShape().getPredicate();
				
				addFromItem(valueOf.getTpropertyShape().getOwner());
				
				ColumnExpression column = new ColumnExpression(sourcePredicate.getLocalName());
				right = result = column;
				
				if (!targetPredicate.getLocalName().equals(sourcePredicate.getLocalName())) {
					result = new AliasExpression(column, targetPredicate.getLocalName());
				}
				
			} else if (e instanceof TFunctionExpression) {
				SqlFunctionExpression sqlFunc = sqlFunctionExpression(p, (TFunctionExpression)e);
				right = sqlFunc;
				result = new AliasExpression(sqlFunc, targetPredicate.getLocalName());
			} else if (e instanceof TIriTemplateExpression) {
				TIriTemplateExpression template = (TIriTemplateExpression)e;
				right = result = iriTemplate(template);
				
				addFromItem(template.valueOf().getOwner());
			} else if (e instanceof TReferenceDataExpression) {
				right = result = new AliasExpression(referenceData((TReferenceDataExpression)e), targetPredicate.getLocalName());
			}

			if (update != null && !isKey(p) && right!=null) {
				
				UpdateItem updateItem = new UpdateItem(targetColumn, right);
				update.add(updateItem);
			}
			
			if (result == null) {
				String msg = (e==null) ?
					"Failed to generate value " + p.getPath() + ": ValueExpression is null" :
					"Failed to generate value " + p.getPath() + " of type " + e.getClass().getSimpleName();
				
				throw new ShapeTransformException(msg);
			}
			
			return result;
		}


		private ValueExpression referenceData(TReferenceDataExpression e) throws ShapeTransformException {
			TPropertyShape p = e.valueOf();
			
			TPropertyShape keyProperty = e.getKeyProperty();
			URI targetPredicate = p.getPropertyGroup().getTargetProperty().getPredicate();
			List<Vertex> individualList = e.getIndividuals();
			
			if (logger.isDebugEnabled()) {
				logger.debug("referenceData: {}", e.toString());
				logger.debug("referenceData: keyProperty={}", keyProperty.toString());
			}

			ValueExpression left = null;
			if (keyProperty instanceof OutTPropertyShape) {
				OutTPropertyShape out = (OutTPropertyShape) keyProperty;
				TPropertyShape base = out.getBaseProperty();
				left = column(base);
			}
			
			if (left == null) {
				throw new ShapeTransformException("Failed to find left operand for " + e.toString());
			}
			
			List<SimpleWhenClause> whenList = new ArrayList<>();
			for (Vertex v : individualList) {
				
				Value rightValue = v.getValue(keyProperty.getPredicate());
				ValueExpression right = valueExpression(rightValue);
				
				Result result = enumValueExpression(v, targetPredicate);
				SimpleWhenClause clause = new SimpleWhenClause(right, result);
				whenList.add(clause);
			}
			
			return new SimpleCase(left, whenList, null);
		}
		
		private Result enumValueExpression(Vertex v, URI targetPredicate) throws ShapeTransformException {
			if (Konig.id.equals(targetPredicate)) {
				if (v.getId() instanceof URI) {
					return new StringLiteralExpression(((URI)v.getId()).getLocalName());
				} else {
					throw new ShapeTransformException("Expected URI identifier but found " + v.getId().stringValue());
				}
			}
			Value value = v.getValue(targetPredicate);
			return valueExpression(value);
		}

		private ValueExpression valueExpression(Value value) throws ShapeTransformException {
			if (value == null) {
				throw new ShapeTransformException("value must be defined");
			}
			if (value instanceof Literal) {
				OwlReasoner reasoner = transform.getOwlReasoner();
				Literal literal = (Literal) value;
				URI type = literal.getDatatype();
				if (type != null) {
					if (reasoner.isRealNumber(type)) {
						return new SignedNumericLiteral(new Double(literal.doubleValue()));
					}
					if (reasoner.isIntegerDatatype(type)) {
						return new SignedNumericLiteral(new Long(literal.longValue()));
					}
					return new StringLiteralExpression(literal.stringValue());
				}

			}
			
			if (value instanceof URI) {
				URI uri = (URI) value;
				return new StringLiteralExpression(uri.getLocalName());
			}
			throw new ShapeTransformException("Cannot convert to ValueExpression: " + value.stringValue());
		}

		private AliasExpression iriTemplate(TIriTemplateExpression e) throws ShapeTransformException {
			
			SqlFunctionExpression func = new SqlFunctionExpression(SqlFunctionExpression.CONCAT);
			for (TIriTemplateItem item : e.getItemList()) {
				func.addArg(iriTemplateItem(item));
			}
			
			return new AliasExpression(func, "id");
		}

		private QueryExpression iriTemplateItem(TIriTemplateItem item) throws ShapeTransformException {
			if (item instanceof TLiteralExpression) {
				return literal((TLiteralExpression)item);
			
			} else if (item instanceof ValueOfExpression) {
				return valueOf((ValueOfExpression)item);
			}
			throw new ShapeTransformException(errorMessage("failed to build QueryExpression from " + item));
		}

		private QueryExpression valueOf(ValueOfExpression item) {
			TPropertyShape p = item.getTpropertyShape();
			return column(p);
		}

		private ColumnExpression column(TPropertyShape p) {
			return new ColumnExpression(p.getPredicate().getLocalName());
		}

		private SqlFunctionExpression sqlFunctionExpression(TPropertyShape p, TFunctionExpression e) throws ShapeTransformException {
			FunctionExpression func = e.getFunctionExpression();
			SqlFunctionExpression sqlFunc = new SqlFunctionExpression(func.getFunctionName());
			for (TExpression arg : e.getArgList()) {
				sqlFunc.addArg(toQueryExpression(arg));
			}
			return sqlFunc;
		}

		

		private QueryExpression toQueryExpression(TExpression arg) throws ShapeTransformException {
			if (arg instanceof ValueOfExpression) {
				return columnExpression((ValueOfExpression)arg);
			} else if (arg instanceof TLiteralExpression) {
				return literal((TLiteralExpression) arg);
			}
			throw new ShapeTransformException(errorMessage("Failed to generate QueryExpression for " + arg));
		}

		private ValueExpression literal(TLiteralExpression arg) throws ShapeTransformException {
			Literal value = arg.getValue();
			return literalValue(value);
		}

		private ValueExpression literalValue(Literal value) throws ShapeTransformException {
			URI datatype = value.getDatatype();
			if (datatype == null || XMLSchema.STRING.equals(datatype)) {
				return new StringLiteralExpression(value.stringValue());
			}
			throw new ShapeTransformException(errorMessage("Failed to create literal value: " + value));
		}

		private QueryExpression columnExpression(ValueOfExpression arg) {
			return new ColumnExpression(arg.getTpropertyShape().getPredicate().getLocalName());
		}

		private boolean isKey(TPropertyShape p) {
			if (p.getPredicate().equals(Konig.id)) {
				return true;
			}
			if (p instanceof SimpleTPropertyShape) {
				SimpleTPropertyShape simple = (SimpleTPropertyShape) p;
				URI stereotype = simple.getConstraint().getStereotype();
				return stereotype!=null && (
						Konig.uniqueKey.equals(stereotype) ||
						Konig.primaryKey.equals(stereotype) || 
						Konig.syntheticKey.equals(stereotype));
			}
			return false;
		}

		private void addFromItem(TNodeShape sourceShape) throws ShapeTransformException {
			TDataSource ds = sourceShape.getTdatasource();
			if (ds == null) {
				throw new ShapeTransformException("DataSource is not defined for shape: " + sourceShape);
			}
			
			if (!dataSources.contains(ds)) {
				dataSources.add(ds);
				// For now, assume a single datasource
				// TODO: support joins
				
				SelectExpression select = transform.getSelectExpression();
				
				TableItemExpression tableItem = tableItemExpression(ds);
				select.getFrom().add(tableItem);
			}
			
		}

		private TableItemExpression tableItemExpression(TDataSource ds) throws ShapeTransformException {
			
			DataSource rawDataSource = ds.getDatasource();
			
			if (rawDataSource instanceof TableDataSource) {
				TableDataSource tds = (TableDataSource) rawDataSource;
				String tableName = tds.getTableIdentifier();
				return new TableNameExpression(tableName);
			}
			
			throw new ShapeTransformException("Failed to create tableItemExpression: " + ds);
		}
	}

	
}
