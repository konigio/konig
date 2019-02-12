package io.konig.transform.showl.sql;

import java.text.MessageFormat;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.showl.NodeNamer;
import io.konig.core.showl.ShowlDerivedPropertyShape;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlFormulaPropertyShape;
import io.konig.core.showl.ShowlJoinCondition;
import io.konig.core.showl.ShowlMapping;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlSourceToSourceJoinCondition;
import io.konig.core.showl.ShowlTemplatePropertyShape;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.FunctionExpression;
import io.konig.formula.LiteralFormula;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.CastSpecification;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueExpression;

public class ShowlSqlTransform {
	private static final Logger logger = LoggerFactory.getLogger(ShowlSqlTransform.class);

	public ShowlSqlTransform() {
	}
	
	public InsertStatement createInsert(ShowlNodeShape targetNode, Class<? extends TableDataSource> datasourceType) throws ShowlSqlTransformException  {
		if (logger.isTraceEnabled()) {
			logger.trace("createInsert({})", targetNode.getPath());
		}
		Worker worker = new Worker();
		return worker.createInsert(targetNode, datasourceType);
	}

	private class Worker {
	
		private NodeNamer nodeNamer = new NodeNamer();
		private Class<? extends TableDataSource> datasourceType;
		
		public InsertStatement createInsert(ShowlNodeShape targetNode, Class<? extends TableDataSource> datasourceType) throws ShowlSqlTransformException  {
			this.datasourceType = datasourceType;
			
			TableNameExpression tableName = tableName(targetNode);
			List<ColumnExpression> columns = insertColumns(targetNode);
			SelectExpression selectQuery = selectInto(targetNode);
			InsertStatement insert = new InsertStatement(tableName, columns, selectQuery);
			
			return insert;
		}

		private SelectExpression selectInto(ShowlNodeShape targetNode) throws ShowlSqlTransformException {
			SelectExpression select = new SelectExpression();
			for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
				ValueExpression value = mappedValue(p);
				if (value != null) {
					select.add(value);
				}
			}
			addFrom(targetNode, select);
			return select;
		}

		private void addFrom(ShowlNodeShape targetNode, SelectExpression select) throws ShowlSqlTransformException {
			
			for (ShowlJoinCondition join : targetNode.getSelectedJoins()) {

				ShowlNodeShape focusNode = join.focusNode();
				TableItemExpression tableItem = tableAlias(focusNode);
				
				if (join instanceof ShowlSourceToSourceJoinCondition) {
					
					OnExpression on = onExpression(join);
					
					
					tableItem = new JoinExpression(tableItem, on);
				}
				select.getFrom().add(tableItem);
				
				
			}
			
		}

		private OnExpression onExpression(ShowlJoinCondition join) throws ShowlSqlTransformException {
			
			ShowlPropertyShape left = join.getLeft();
			ShowlPropertyShape right = join.getRight();
			
			if (left instanceof ShowlTemplatePropertyShape && right instanceof ShowlTemplatePropertyShape) {
				ShowlTemplatePropertyShape leftT = (ShowlTemplatePropertyShape) left;
				ShowlTemplatePropertyShape rightT = (ShowlTemplatePropertyShape) right;
				
				// We handle the special case where the templates are equal.
				// There are other special cases we should handle, such as the case
				// where the templates are the same except for variables that are equivalent
				// by mapping.  But that's too complicated for now.  
				
				if (leftT.getTemplate().equals(rightT.getTemplate())) {
					String leftTableAlias = nodeNamer.varname(left.getDeclaringShape());
					String rightTableAlias = nodeNamer.varname(right.getDeclaringShape());
					List<? extends ValueFormat.Element> elements = leftT.getTemplate().toList();
					BooleanTerm booleanTerm = null;
					AndExpression andExpression=null;
					Context leftContext = leftT.getTemplate().getContext();
					leftContext.compile();
					
					for (ValueFormat.Element e : elements) {
						if (e.getType() == ElementType.VARIABLE) {
							String varName = e.getText();
							
							URI predicate = leftContext.getTerm(varName).getExpandedId();
							
							ShowlPropertyShape leftProperty = left.getDeclaringShape().findProperty(predicate);
							ShowlPropertyShape rightProperty = right.getDeclaringShape().findProperty(predicate);
							
							ValueExpression leftCol = valueExpression(leftProperty);
							ValueExpression rightCol = valueExpression(rightProperty);
							
							ComparisonPredicate compare = new ComparisonPredicate(
								ComparisonOperator.EQUALS, leftCol, rightCol);
							
							if (andExpression != null) {
								andExpression.add(compare);
							} else if (booleanTerm != null) {
								andExpression = new AndExpression();
								andExpression.add(booleanTerm);
								andExpression.add(compare);
								booleanTerm = andExpression;
							} else {
								booleanTerm = compare;
							}
							
						}
					}
					return new OnExpression(booleanTerm);
				} else {
					ValueExpression leftValue = templateValue(leftT);
					ValueExpression rightValue = templateValue(rightT);
					return new OnExpression(new ComparisonPredicate(
							ComparisonOperator.EQUALS,
							leftValue,
							rightValue));
				}
				
			}
			
			ComparisonPredicate compare = new ComparisonPredicate(
					ComparisonOperator.EQUALS,
					valueExpression(left),
					valueExpression(right));
				
			return new OnExpression(compare);
		}

		private ValueExpression valueExpression(ShowlPropertyShape p) {
			if (p instanceof ShowlDerivedPropertyShape) {
				ShowlPropertyShape peer = p.getPeer();
				if (peer != null) {
					return qualifiedColumn(peer);
				}
			}
			return qualifiedColumn(p);
		}


		private TableAliasExpression tableAlias(ShowlNodeShape node) throws ShowlSqlTransformException {
			TableNameExpression tableName = tableName(node);
			String alias = nodeNamer.varname(node);
			
			return new TableAliasExpression(tableName, alias);
		}
		

		private ValueExpression mappedValue(ShowlDirectPropertyShape p) throws ShowlSqlTransformException {
			
			ShowlNodeShape valueShape = p.getValueShape();
			if (valueShape != null) {
				return struct(valueShape);
			}
			
			ShowlMapping m = p.getSelectedMapping();
			if (m == null) {
				return null;
			}
			ShowlPropertyShape other = m.findOther(p);
			if (other instanceof ShowlTemplatePropertyShape) {
				return templateValue((ShowlTemplatePropertyShape) other);
			}
			
			if (p.getPropertyConstraint().getNodeKind() == NodeKind.IRI) {
				return iriReference(p, other);
			}
			
		
			if (other instanceof ShowlFormulaPropertyShape) {
				return alias(formula((ShowlFormulaPropertyShape)other), p);
			}
			
			
			String tableAlias = nodeNamer.varname(other.getDeclaringShape());
			
			String sourceColumnName = other.getPredicate().getLocalName();
			String targetColumnName = p.getPredicate().getLocalName();
			ColumnExpression column = new ColumnExpression(tableAlias + "." + sourceColumnName);
			if (!targetColumnName.equals(sourceColumnName)) {
				return new AliasExpression(column, targetColumnName);
			}
			
			return column;
		}

		

		private ValueExpression iriReference(ShowlDirectPropertyShape p, ShowlPropertyShape other) throws ShowlSqlTransformException {
			
			ShowlNodeShape otherNode = other.getValueShape();
			if (otherNode != null) {
				ShowlPropertyShape otherId = otherNode.findProperty(Konig.id);
				if (otherId instanceof ShowlTemplatePropertyShape) {
					ValueExpression e = templateValue((ShowlTemplatePropertyShape)otherId);
					
					return new AliasExpression(e, p.getPredicate().getLocalName());
				}
			}
			
			throw new ShowlSqlTransformException(
				MessageFormat.format("Failed to construct IRI reference for {0} from {1}",
						p.getPath(), other.getPath()));
		}

		private ValueExpression alias(ValueExpression e, ShowlDirectPropertyShape p) {
			String aliasName = p.getPredicate().getLocalName();
			return new AliasExpression(e, aliasName);
		}

		private ValueExpression formula(ShowlFormulaPropertyShape p) throws ShowlSqlTransformException {
			QuantifiedExpression formula = p.getPropertyConstraint().getFormula();
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof FunctionExpression) {
				return function(p.getDeclaringShape(), (FunctionExpression)primary);
			}
			String msg = MessageFormat.format("For property {0}, failed to generate SQL from {1}", p.getPath(), formula.getText());
			throw new ShowlSqlTransformException(msg);
		}

		

		private ValueExpression function(ShowlNodeShape node, FunctionExpression function) throws ShowlSqlTransformException {
			String functionName = function.getFunctionName();
			boolean castToString = SqlFunctionExpression.CONCAT.equals(functionName);
			List<QueryExpression> argList = argList(node, function.getArgList(), castToString);
			SqlFunctionExpression sql = new SqlFunctionExpression(functionName, argList);
			
			return sql;
		}

		private List<QueryExpression> argList(ShowlNodeShape node, List<Expression> argList, boolean castToString) throws ShowlSqlTransformException {
			List<QueryExpression> list = new ArrayList<>();
			for (Expression e : argList) {
				PrimaryExpression primary = e.asPrimaryExpression();
				if (primary instanceof LiteralFormula) {
					list.add(stringLiteral((LiteralFormula)primary));
				} else if (primary instanceof PathExpression) {
					// TODO: cast to string if necessary
					list.add(path(node, (PathExpression)primary, castToString));
				} 
				logger.trace(primary.getClass().getSimpleName());
			}
			
			return list;
		}

		private QueryExpression path(ShowlNodeShape node, PathExpression path, boolean castToString) throws ShowlSqlTransformException {
			StringBuilder builder = new StringBuilder();
			String nodeAlias = nodeNamer.varname(node);
			builder.append(nodeAlias);
			
			ShowlPropertyShape p = null;
			
			for (PathStep step : path.getStepList()) {
				if (step instanceof DirectionStep) {
					DirectionStep dirStep = (DirectionStep) step;
					if (dirStep.getDirection() == Direction.OUT) {
						URI predicate = dirStep.getTerm().getIri();
						String localName = predicate.getLocalName();
						builder.append('.');
						builder.append(localName);
						
						if (castToString) {
							ShowlPropertyShape q = null;
							if (p == null) {
								q = node.findProperty(predicate);
							} else if (p.getValueShape()!=null){
								q = p.getValueShape().findProperty(predicate);
							}
							
							if (q == null) {
								
								String basePath = p==null ? node.getPath() : p.getPath();
								
								throw ShowlSqlTransformException.format(
									"Property not found {0}.{1}", basePath, predicate.getLocalName());
							}
							p = q;
						}
						
					} else {
						throw new ShowlSqlTransformException("IN steps not supported yet in path" + path.simpleText());
					}
					
				} else {
					throw new ShowlSqlTransformException("Path not supported: " + path.simpleText());
				}
			}
			
			ColumnExpression column = new ColumnExpression(builder.toString());
			
			if (castToString) {
				return castToString(p, column);
			}
			
			return column;
		}

		private QueryExpression castToString(ShowlPropertyShape p, ColumnExpression column) {
			PropertyConstraint constraint = p.getPropertyConstraint();
			if (constraint != null) {
				URI datatype = constraint.getDatatype();
				if (XMLSchema.DATE.equals(datatype)) {
					SqlFunctionExpression func = new SqlFunctionExpression("FORMAT_DATE");
					func.addArg(new StringLiteralExpression("%Y-%m-%d"));
					func.addArg(column);
					return func;
				} else if (XMLSchema.DATETIME.equals(datatype)) {
					SqlFunctionExpression func = new SqlFunctionExpression("FORMAT_DATETIME");
					func.addArg(new StringLiteralExpression("%Y-%m-%dT%TZ"));
					func.addArg(column);
					return func;
					
				} else if (
					XMLSchema.BOOLEAN.equals(datatype) ||
					XMLSchema.FLOAT.equals(datatype) ||
					XMLSchema.DOUBLE.equals(datatype) ||
					XMLSchema.DECIMAL.equals(datatype) ||
					XMLSchema.INTEGER.equals(datatype) ||
					XMLSchema.NON_POSITIVE_INTEGER.equals(datatype) ||
					XMLSchema.LONG.equals(datatype) ||
					XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
					XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
					XMLSchema.INT.equals(datatype) ||
					XMLSchema.UNSIGNED_LONG.equals(datatype) ||
					XMLSchema.POSITIVE_INTEGER.equals(datatype) ||
					XMLSchema.SHORT.equals(datatype) ||
					XMLSchema.UNSIGNED_INT.equals(datatype) ||
					XMLSchema.BYTE.equals(datatype) ||
					XMLSchema.UNSIGNED_SHORT.equals(datatype) ||
					XMLSchema.UNSIGNED_BYTE.equals(datatype)
				) {
					return new CastSpecification(column, "STRING");
				}
			}
			return column;
		}

		private StringLiteralExpression stringLiteral(LiteralFormula e) throws ShowlSqlTransformException {
			Literal literal = e.getLiteral();
			if (literal.getDatatype()==null || XMLSchema.STRING.equals(literal.getDatatype())) {
				return new StringLiteralExpression(literal.stringValue());
			}
			throw new ShowlSqlTransformException("Failed to produce string literal from " + e.getLiteral().toString());
		}

		private ValueExpression struct(ShowlNodeShape node) throws ShowlSqlTransformException {
			StructExpression struct = new StructExpression();
			for (ShowlDirectPropertyShape p : node.getProperties()) {
				ValueExpression v = mappedValue(p);
				if (v == null) {
					throw new ShowlSqlTransformException("Value not mapped: " + p.getPath());
				}
				struct.add(v);
			}
			String fieldName = node.getAccessor().getPredicate().getLocalName();
			
		
			
			return new AliasExpression(struct, fieldName);
		}

		private ValueExpression templateValue(ShowlTemplatePropertyShape showlTemplate) throws ShowlSqlTransformException {
			ShowlNodeShape node = showlTemplate.getDeclaringShape();
			String tableAlias = nodeNamer.varname(node);
			IriTemplate template = showlTemplate.getTemplate();
			SqlFunctionExpression func = new SqlFunctionExpression(SqlFunctionExpression.CONCAT);

			Context context = template.getContext();
			
			for (ValueFormat.Element e : template.toList()) {
				switch (e.getType()) {
				case TEXT:
					func.addArg(new StringLiteralExpression(e.getText()));
					break;
					
				case VARIABLE:
					String fieldName = e.getText();
					
					URI predicate = new URIImpl(context.expandIRI(fieldName));
					
					ShowlPropertyShape p = node.findProperty(predicate);
					if (p == null) {
						String msg = MessageFormat.format("Template variable {0} not found for {1}", 
								fieldName, showlTemplate.getPath());
						throw new ShowlSqlTransformException(msg);
					}
					
					String fullName = null;
					if (p instanceof ShowlDerivedPropertyShape) {
						ShowlPropertyShape peer = p.getPeer();
						fullName = tableAlias + "." + peer.getPredicate().getLocalName();
					} else {
					
						fullName = tableAlias + "." + fieldName;
					}
					
					
					ColumnExpression column = new ColumnExpression(fullName);
					func.addArg(column);
					break;
					
				}
			}
			String targetName = showlTemplate.getPredicate().getLocalName();
			
			return new AliasExpression(func, targetName);
		}
		
		
		
		private ColumnExpression qualifiedColumn(ShowlPropertyShape p) {
			String tableName = nodeNamer.varname(p.getDeclaringShape());
			String columnName = tableName + "." + p.getPredicate().getLocalName();
			return new ColumnExpression(columnName);
			
		}

		private List<ColumnExpression> insertColumns(ShowlNodeShape targetNode) {
			List<ColumnExpression> list = new ArrayList<>();
			for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
				String columnName = p.getPredicate().getLocalName();
				list.add(new ColumnExpression(columnName));
			}
			return list;
		}

		private TableNameExpression tableName(ShowlNodeShape targetNode) throws ShowlSqlTransformException {
		
			for (DataSource ds : targetNode.getRoot().getShape().getShapeDataSource()) {
				if (datasourceType.isInstance(ds)) {
					TableDataSource table = (TableDataSource) ds;
					String tableName = table.getQualifiedTableName();
					return new TableNameExpression(tableName);
				}
				
			}
			throw new ShowlSqlTransformException(
					"Datasource of type " + datasourceType.getSimpleName() + " not found in  " + targetNode.getPath());
		}
	}
}
