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
import io.konig.core.KonigException;
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
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.Addend;
import io.konig.formula.AdditiveOperator;
import io.konig.formula.BinaryOperator;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.BuiltInName;
import io.konig.formula.CaseStatement;
import io.konig.formula.DateTruncFunctionModel;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.Formula;
import io.konig.formula.FullyQualifiedIri;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.KqlType;
import io.konig.formula.LiteralFormula;
import io.konig.formula.LocalNameTerm;
import io.konig.formula.MultiplicativeExpression;
import io.konig.formula.ParameterModel;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.UnaryExpression;
import io.konig.formula.VariableTerm;
import io.konig.formula.WhenThenClause;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.AdditiveValueExpression;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.CastSpecification;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.CountStar;
import io.konig.sql.query.DateTimeUnitExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.NumericValueExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.Result;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.SqlDialect;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueExpression;

public class ShowlSqlTransform {
	private static final Logger logger = LoggerFactory.getLogger(ShowlSqlTransform.class);


	private static final FunctionModel DATE_TRUNC = 
		new FunctionModel("DATE_TRUNC", KqlType.INSTANT)
		.param("temporalUnit", KqlType.STRING)
		.param("temporalValue", KqlType.INSTANT);

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
		private SqlDialect dialect;
		
		public InsertStatement createInsert(ShowlNodeShape targetNode, Class<? extends TableDataSource> datasourceType) throws ShowlSqlTransformException  {
			this.datasourceType = datasourceType;
			setDialect();
			
			TableNameExpression tableName = tableName(targetNode);
			List<ColumnExpression> columns = insertColumns(targetNode);
			SelectExpression selectQuery = selectInto(targetNode);
			InsertStatement insert = new InsertStatement(tableName, columns, selectQuery);
			
			return insert;
		}

		private void setDialect() {
			if (GoogleBigQueryTable.class.isAssignableFrom(datasourceType)) {
				dialect = SqlDialect.BIGQUERY;
			} else {
				dialect = SqlDialect.MYSQL;
			}
			
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
			
			if (other instanceof ShowlFormulaPropertyShape) {
				QuantifiedExpression formula = other.getPropertyConstraint().getFormula();
				return valueExpression(other, formula);
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
			ValueExpression v = valueExpression(p, formula);
			if (v == null) {
				String msg = MessageFormat.format("For property {0}, failed to generate SQL from {1}", p.getPath(), formula.getText());
				throw new ShowlSqlTransformException(msg);
			}
			return v;
		}
		
		

		

		private ValueExpression caseStatement(ShowlPropertyShape p, CaseStatement kql) throws ShowlSqlTransformException {
			
			ValueExpression caseOperand = caseOperand(p, kql);
			List<SimpleWhenClause> whenClauseList = whenList(p, kql);
			Result elseClause = elseClause(p, kql);
			
			return new SimpleCase(caseOperand, whenClauseList, elseClause);
		}

		private ValueExpression elseClause(ShowlPropertyShape p, CaseStatement kql) throws ShowlSqlTransformException {
			Expression e = kql.getElseClause();
			return e==null ? null : valueExpression(p, e);
		}

		private ValueExpression caseOperand(ShowlPropertyShape p, CaseStatement kql) throws ShowlSqlTransformException {
			Expression e = kql.getCaseCondition();
			return e==null ? null : valueExpression(p, e);
		}

		private List<SimpleWhenClause> whenList(ShowlPropertyShape p, CaseStatement kql) throws ShowlSqlTransformException {
			List<SimpleWhenClause> result = new ArrayList<>();
			for (WhenThenClause c : kql.getWhenThenList()) {
				result.add(whenThenClause(p, c));
			}
			return result;
		}

		private SimpleWhenClause whenThenClause(ShowlPropertyShape p, WhenThenClause c) throws ShowlSqlTransformException {
			ValueExpression whenOperand = valueExpression(p, c.getWhen());
			Result result = valueExpression(p, c.getThen());
			return new SimpleWhenClause(whenOperand, result);
		}

		
		private ValueExpression valueExpression(ShowlPropertyShape p, Formula e) throws ShowlSqlTransformException {

			PrimaryExpression primary = e.asPrimaryExpression();
			if (primary instanceof PathExpression) {
				return path(p, (PathExpression) primary, kqlType(p));
			} else if (primary instanceof FunctionExpression) {
				return function(p, (FunctionExpression)primary);
			} else if (primary instanceof CaseStatement) {
				return caseStatement(p, (CaseStatement)primary);
			} else if (primary instanceof LocalNameTerm) {
				return localNameTerm(p, (LocalNameTerm) primary);
			}
			
			BinaryRelationalExpression binary = e.asBinaryRelationalExpression();
			if (binary != null && binary.getRight() != null) {
				return binaryRelationalExpression(p, binary);
			}
			if (e instanceof GeneralAdditiveExpression) {
				return additive(p, null, (GeneralAdditiveExpression)e);
			}
			
			fail(p, "Failed to generate SQL for {0}", e.toString());
			
			return null;
		}

		

		private ValueExpression localNameTerm(ShowlPropertyShape p, LocalNameTerm term) {
			URI iri = term.getIri();
			return new StringLiteralExpression(iri.getLocalName());
		}

		private ComparisonOperator comparisonOperator(BinaryOperator operator) {
			switch (operator) {
			case EQUALS: return ComparisonOperator.EQUALS;
			case GREATER_THAN: return ComparisonOperator.GREATER_THAN;
			case GREATER_THAN_OR_EQUAL: return ComparisonOperator.GREATER_THAN_OR_EQUALS;
			case LESS_THAN: return ComparisonOperator.LESS_THAN;
			case LESS_THAN_OR_EQUAL: return ComparisonOperator.LESS_THAN_OR_EQUALS;
			case NOT_EQUAL: return ComparisonOperator.NOT_EQUALS;
			}
			throw new KonigException("Unsupported comparison operator: " + operator.getText());
		}


		private ValueExpression binaryRelationalExpression(ShowlPropertyShape p, BinaryRelationalExpression binary) throws ShowlSqlTransformException {

			ValueExpression left = valueExpression(p, binary.getLeft());
			ComparisonOperator op = comparisonOperator(binary.getOperator());
			ValueExpression right = valueExpression(p, binary.getRight());
			return new ComparisonPredicate(op, left, right);
		}

		private void fail(ShowlPropertyShape p, String pattern, Object...arg) throws ShowlSqlTransformException {
			String left = "At " + p.getPath() + " ... ";
			String right = MessageFormat.format(pattern, arg);
			throw new ShowlSqlTransformException(left + right);
		}

		private ValueExpression function(ShowlPropertyShape p, FunctionExpression function) throws ShowlSqlTransformException {
			
			SqlFunctionExpression sql = dateTrunc(p, function);
			if (sql == null) {
				String functionName = function.getFunctionName();
				
				List<QueryExpression> argList = argList(p, function.getArgList(), function.getModel());
				sql = new SqlFunctionExpression(functionName, argList);
			}
			
			return sql;
		}

		

		

		private SqlFunctionExpression dateTrunc(ShowlPropertyShape p, FunctionExpression function) throws ShowlSqlTransformException {
			if (function.getModel() instanceof DateTruncFunctionModel) {
				String timeUnit = function.getFunctionName();
				if (function.getArgList().size() != 1) {
					fail(p, "Expected 1 argument for {0} function but found {1}", 
							function.getFunctionName(), function.toString());
				}
				ValueExpression value = valueExpression(p, function.getArgList().get(0));
				List<QueryExpression> argList = new ArrayList<>();
				switch (dialect) {
				case BIGQUERY :
					argList.add(value);
					argList.add(new DateTimeUnitExpression(timeUnit));
					break;
					
				default:
					argList.add(new StringLiteralExpression(timeUnit));
					argList.add(value);
				}
				return new SqlFunctionExpression("DATE_TRUNC", argList);
			}
			
			return null;
		}

		private List<QueryExpression> argList(ShowlPropertyShape p,  List<Expression> argList, FunctionModel model) throws ShowlSqlTransformException {
			
			List<ParameterModel> paramList = model.getParameters();
			
			List<QueryExpression> list = new ArrayList<>();
			int paramIndex = 0;
			for (Expression e : argList) {
				if (paramIndex >= paramList.size()) {
					String msg = MessageFormat.format("Invalid number of arguments to function {0}", model.getName());
					throw new ShowlSqlTransformException(msg);
				}
				
				ParameterModel param = paramList.get(paramIndex);
				if (!param.isEllipsis()) {
					paramIndex++;
				}
				
				PrimaryExpression primary = e.asPrimaryExpression();
				if (primary instanceof LiteralFormula) {
					list.add(literal((LiteralFormula)primary));
				} else if (primary instanceof PathExpression) {
					// TODO: cast to string if necessary
					list.add(path(p, (PathExpression)primary, param.getType()));
				} else if (primary == null) {
					GeneralAdditiveExpression additive = e.asAdditiveExpression();
					if (additive != null) {
						list.add(additive(p, param.getType(), additive));
					}
				} 
			}
			
			return list;
		}

		private ValueExpression additive(ShowlPropertyShape p, KqlType type, GeneralAdditiveExpression additive) throws ShowlSqlTransformException {
			ValueExpression left = multiplicative(p, type, additive.getLeft());
			List<Addend> addendList = additive.getAddendList();
			if (addendList != null) {
				for (Addend a : addendList) {
					AdditiveOperator op = a.getOperator();
					ValueExpression right = multiplicative(p, type, a.getRight());
					left = new AdditiveValueExpression(op, left, right);
				}
			}
			
			return left;
		}

		private ValueExpression multiplicative(ShowlPropertyShape p, KqlType type, MultiplicativeExpression source) throws ShowlSqlTransformException {
			UnaryExpression unary = source.getLeft();
			if (unary.getOperator() != null) {
				throw new KonigException("Unary operators not supported");
			}
			if (source.getMultiplierList() != null && !source.getMultiplierList().isEmpty()) {
				throw new KonigException("Multipliers not supported");
			}
			PrimaryExpression primary = unary.getPrimary();
			if (primary instanceof LiteralFormula) {
				return literal((LiteralFormula) primary);
			}
			
			
			if (primary instanceof PathExpression) {
				QueryExpression e = path(p, (PathExpression) primary, type);
				if (e instanceof NumericValueExpression) {
					return (NumericValueExpression) e;
				}
				throw new KonigException("Expected a NumericValueExpression but found " + e.getClass().getSimpleName());
			}
			
			if (primary instanceof FunctionExpression) {
				FunctionExpression func = (FunctionExpression) primary;
				
				if (isCountStar(func)) {
					return new CountStar();
				}
				return function(p, (FunctionExpression)primary);
			}
			if (primary instanceof FullyQualifiedIri) {
				FullyQualifiedIri full = (FullyQualifiedIri) primary;
				URI iri = full.getIri();
				return new StringLiteralExpression(iri.getLocalName());
			}
			if (primary instanceof LocalNameTerm) {
				LocalNameTerm local = (LocalNameTerm) primary;
				return new StringLiteralExpression(local.getLocalName());
			}
			if (primary instanceof BuiltInName) {
				BuiltInName builtIn = (BuiltInName) primary;
				// TODO: confirm that the built-in name is a date-time unit.
				return new DateTimeUnitExpression(builtIn.getIri().getLocalName());
			}
			
			throw new KonigException("Expression type not supported: " + primary.getClass().getSimpleName());
		}

		private boolean isCountStar(FunctionExpression func) {
			if (FunctionExpression.COUNT.equalsIgnoreCase(func.getFunctionName())) {
				
				Expression arg = func.getArgList().get(0);
				
				PrimaryExpression primary = arg.asPrimaryExpression();
				if (primary instanceof PathExpression) {
					
					PathExpression path = (PathExpression) primary;
					List<PathStep> stepList = path.getStepList();
					if (stepList.size()==1) {
						PathStep step = stepList.get(0);
						if (step instanceof DirectionStep) {
							DirectionStep dir = (DirectionStep) step;
							if (dir.getDirection() == Direction.OUT) {
								PathTerm term = dir.getTerm();
								if (term instanceof VariableTerm) {
									return true;
								}
							}
						}
					}
				}
				
			}
			return false;
		}

		private ValueExpression path(ShowlPropertyShape property, PathExpression path, KqlType expectedType) throws ShowlSqlTransformException {
			ShowlNodeShape node = property.getDeclaringShape();
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
						
					} else {
						throw new ShowlSqlTransformException("IN steps not supported yet in path" + path.simpleText());
					}
					
				} else {
					throw new ShowlSqlTransformException("Path not supported: " + path.simpleText());
				}
			}
			
			ColumnExpression column = new ColumnExpression(builder.toString());
			return cast(expectedType, p, column);
		}
		
	
		private ValueExpression cast(KqlType expectedType, ShowlPropertyShape p, ValueExpression e) throws ShowlSqlTransformException {
			
			if (expectedType == null) {
				return e;
			}
			KqlType actualType = kqlType(p);
			if (actualType == null) {
				String msg = MessageFormat.format("KQL type of property not known: {0}", p.getPath());
				throw new ShowlSqlTransformException(msg);
				
			}
			if (actualType.equals(expectedType)) {
				return e;
			}
			
			switch (expectedType) {
			case STRING :
				return castToString(p, e);
				
			case INTEGER :
				return castToInteger(actualType, p, e);
				
			default:
				String msg = MessageFormat.format("Don't know how to cast property {0}", p.getPath());
				throw new ShowlSqlTransformException(msg);
			}
		}

		

		private ValueExpression castToInteger(KqlType kqlType, ShowlPropertyShape p, ValueExpression e) throws ShowlSqlTransformException {
			switch(kqlType) {
			case STRING :
				return new CastSpecification(e, "INT");
				
			default:
				String msg = MessageFormat.format("Cannot cast to integer: {0}",  p.getPath());
				throw new ShowlSqlTransformException(msg);
			}
		}

		private KqlType kqlType(ShowlPropertyShape p) {
			if (p == null) {
				return null;
			}
			
			if (p instanceof ShowlDirectPropertyShape) {
				PropertyConstraint c = p.getPropertyConstraint();
				URI datatype = c.getDatatype();
				if (datatype != null) {
					return ShowlUtil.kqlType(datatype);
				}
			} else {
				ShowlPropertyShape peer = p.getPeer();
				if (peer != null) {
					PropertyConstraint c = peer.getPropertyConstraint();
					URI datatype = c.getDatatype();
					if (datatype != null) {
						return ShowlUtil.kqlType(datatype);
					}
				} else {
					PropertyConstraint c = p.getPropertyConstraint();
					if (c != null) {
						QuantifiedExpression q = c.getFormula();
						if (q != null) {
							PrimaryExpression primary = q.asPrimaryExpression();
							if (primary instanceof FunctionExpression) {
								FunctionExpression func = (FunctionExpression) primary;
								return func.getModel().getReturnType();
							}
						}
					}
				}
			}
			
			return null;
		}

		private ValueExpression castToString(ShowlPropertyShape p, ValueExpression column) {
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

		private ValueExpression literal(LiteralFormula e) throws ShowlSqlTransformException {
			Literal literal = e.getLiteral();
			KqlType type = ShowlUtil.kqlType(literal.getDatatype());
			switch (type) {
			case STRING :
				return new StringLiteralExpression(literal.stringValue());
			case INTEGER :
				return new SignedNumericLiteral(literal.longValue());
				
			case NUMBER :
				return new SignedNumericLiteral(literal.doubleValue());
				
			default:
				throw new ShowlSqlTransformException("Failed to produce literal from " + e.getLiteral().toString());
			}
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
