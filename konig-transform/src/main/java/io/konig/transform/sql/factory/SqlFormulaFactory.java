package io.konig.transform.sql.factory;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.core.pojo.BeanUtil;
import io.konig.formula.Addend;
import io.konig.formula.AdditiveOperator;
import io.konig.formula.BinaryOperator;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.BuiltInName;
import io.konig.formula.ConditionalAndExpression;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.FullyQualifiedIri;
import io.konig.formula.FunctionExpression;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.IfFunction;
import io.konig.formula.IriValue;
import io.konig.formula.LiteralFormula;
import io.konig.formula.LocalNameTerm;
import io.konig.formula.MultiplicativeExpression;
import io.konig.formula.NumericExpression;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.UnaryExpression;
import io.konig.formula.ValueLogical;
import io.konig.formula.VariableTerm;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.AdditiveValueExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.CountStar;
import io.konig.sql.query.DateTimeUnitExpression;
import io.konig.sql.query.IfExpression;
import io.konig.sql.query.NumericValueExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.ShapeRule;

public class SqlFormulaFactory {
	private static final Logger logger = LoggerFactory.getLogger(SqlFormulaFactory.class);
	
	public SqlFormulaFactory() {
		
	}

	public ValueExpression formula(SqlFormulaExchange request) throws TransformBuildException {
		Worker worker = new Worker(request);
		
		return worker.formula(request.getProperty());
	}
	
	private static class Worker {

		private VariableTableMap tableMap;
		private PropertyConstraint propertyConstraint;
		private SqlFormulaExchange exchange;
		
		public Worker(SqlFormulaExchange request) {
			this.exchange = request;
			this.tableMap = request.getTableMap();
			this.propertyConstraint = request.getProperty();
		}

		public ValueExpression formula(PropertyConstraint p) throws TransformBuildException {
			
			
			Expression source = p.getFormula();
			if (source == null) {
				throw new TransformBuildException("The 'formula' must be defined for property " + p.getPredicate().stringValue());
			}

			
			try {
				return valueExpression(source);
			} catch (Throwable e) {
				String predicate = p.getPredicate()==null ? "undefined" : p.getPredicate().stringValue();
				throw new TransformBuildException("Failed to generate formula for property " +
						predicate, e
				);
			}
		}

		private ValueExpression valueExpression(Expression source) throws ShapeTransformException {
			
			List<ConditionalAndExpression> orList = source.getOrList();
			
			if (orList==null || orList.isEmpty()) {
				throw new KonigException("Invalid expression");
			}
			
			if (orList.size() != 1) {
				throw new KonigException("Unsupported expression");
			}
			ConditionalAndExpression and = orList.get(0);
			
			return andExpression(and);
		}

		private ValueExpression andExpression(ConditionalAndExpression and) throws ShapeTransformException {
			List<ValueLogical> valueLogicalList = and.getAndList();
			if (valueLogicalList.size() == 1) {
				ValueLogical valueLogical = valueLogicalList.get(0);
				return valueLogical(valueLogical);
			} 
			throw new KonigException("Unsupported expression");
			
		}

		private ValueExpression valueLogical(ValueLogical valueLogical) throws ShapeTransformException {

			if (valueLogical instanceof BinaryRelationalExpression) {
				BinaryRelationalExpression binary = (BinaryRelationalExpression) valueLogical;
				return binaryRelational(binary);
			} 
			
			throw new KonigException("Unsupported expression");
			
		}

		private ValueExpression binaryRelational(BinaryRelationalExpression binary) throws ShapeTransformException {
			ValueExpression left = valueExpression(binary.getLeft());
			
			if (binary.getRight()==null) {
				return left;
			} else {
				ValueExpression right = valueExpression(binary.getRight());
				ComparisonOperator op = comparisonOperator(binary.getOperator());
				return new ComparisonPredicate(op, left, right);
			}
			
		}

		private ValueExpression valueExpression(NumericExpression e) throws ShapeTransformException {

			if (e instanceof GeneralAdditiveExpression) {
				GeneralAdditiveExpression additive = (GeneralAdditiveExpression) e;
				return additiveExpression(additive);
			} else {
				throw new KonigException("Unsupported expression type: " + e.getClass().getSimpleName());
			}
		}

		static private ComparisonOperator comparisonOperator(BinaryOperator operator) {
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

		private ValueExpression additiveExpression(GeneralAdditiveExpression additive) throws ShapeTransformException {
			
			ValueExpression left = multiplicativeExpression(additive.getLeft());
			List<Addend> addendList = additive.getAddendList();
			if (addendList != null) {
				for (Addend a : addendList) {
					AdditiveOperator op = a.getOperator();
					ValueExpression right = multiplicativeExpression(a.getRight());
					left = new AdditiveValueExpression(op, left, right);
				}
			}
			
			return left;
		}

		private ValueExpression multiplicativeExpression(MultiplicativeExpression source) throws ShapeTransformException {
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
			
			if (primary instanceof IfFunction) {
				return ifFunction((IfFunction)primary);
			}
			
			if (primary instanceof PathExpression) {
				QueryExpression e = path((PathExpression) primary);
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
				String funcName = func.getFunctionName();
				
				io.konig.sql.query.SqlFunctionExpression sqlFunc = new io.konig.sql.query.SqlFunctionExpression(funcName);
				addArguments(sqlFunc, func);
				return sqlFunc;
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

		private void addArguments(io.konig.sql.query.SqlFunctionExpression sqlFunc, FunctionExpression sparqlFunc) throws ShapeTransformException {
			
			for (Expression e : sparqlFunc.getArgList()) {
				ValueExpression ve = valueExpression(e);
				sqlFunc.addArg(ve);
			}
			
		}
		
		
		private QueryExpression path(PathExpression primary) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("path({})", primary.simpleText());
			}
			
			List<PathStep> stepList = primary.getStepList();
			if (stepList != null && !stepList.isEmpty()) {
				PathStep first = stepList.get(0);
				
				if (first instanceof DirectionStep) {
					DirectionStep firstDir = (DirectionStep) first;
					if (firstDir.getDirection() == Direction.OUT) {
						
						StringBuilder builder = new StringBuilder();
						int startIndex = 0;
						PathTerm firstTerm = firstDir.getTerm();

						boolean pathStartsWithVariable = false;
						DataChannel channel = null;
						ShapeRule shapeRule = null;
						if (firstTerm instanceof VariableTerm) {
							startIndex = 1;
							pathStartsWithVariable = true;
							VariableTerm var = (VariableTerm) firstTerm;
							String varName = var.getVarName();
							TableItemExpression tableItem = tableMap.tableForVariable(varName);
							if (tableItem == null) {
								throw new KonigException("Table not found for variable in expression: " + primary.toString());
							}
							if (tableItem instanceof TableAliasExpression) {
								TableAliasExpression tableAlias = (TableAliasExpression) tableItem;
								builder.append(tableAlias.getAlias());
							}
						} else {
							// Not a VariableTerm. 
							
							shapeRule = exchange.getShapeRule();
							channel = appendChannel(builder, shapeRule, stepList, firstTerm);
							if (channel != null) {
								startIndex = 1;
							}
						}
						
						
						boolean ok = true;
						for (int i=startIndex; i<stepList.size() && ok; i++) {
							ok = false;
							PathStep step = stepList.get(i);
							if (step instanceof DirectionStep) {
								DirectionStep dir = (DirectionStep) step;
								if (dir.getDirection()==Direction.OUT) {
									PathTerm term = dir.getTerm();
									if (term instanceof IriValue) {
										IriValue value = (IriValue) term;
										URI predicate = value.getIri();
										
										PropertyRule propertyRule = propertyRule(shapeRule, predicate);
										
										channel = channel(channel, propertyRule);
										
										shapeRule = nestedShapeRule(propertyRule);
										
										if (builder.length()>0) {
											builder.append('.');
										} else {
											appendTable(builder, channel);
										}
										builder.append(predicate.getLocalName());
										ok = true;
									}
								}
							} 
						}
						if (ok) {
							ColumnExpression column = new ColumnExpression(builder.toString());
							ValueExpression result = column;
							if (pathStartsWithVariable) {
								exchange.setGroupingElement(column);
								
								Integer maxCount = propertyConstraint.getMaxCount();
								if (maxCount==null) {
									// The value is an array, so we need to use the
									// ARRAY_AGG function.
									
									// But why is this logic specific to the case where the
									// path starts with a variable.  Shouldn't this logic apply
									// in all cases?
									
									io.konig.sql.query.SqlFunctionExpression func = new io.konig.sql.query.SqlFunctionExpression("ARRAY_AGG");
									func.addArg(column);
									result = func;
								}
							}
							return result;
						}
					}
				}
				
			}
			

			throw new KonigException("Unsupported expression: " + primary.toString());
		}

		


		private DataChannel appendChannel(StringBuilder builder, ShapeRule shapeRule, List<PathStep> stepList,
				PathTerm firstTerm) {
			DataChannel channel = null;
			if (firstTerm instanceof IriValue && stepList.size()>1 && shapeRule!=null) {
				IriValue value = (IriValue) firstTerm;
				URI predicate = value.getIri();
				PropertyRule p = shapeRule.getProperty(predicate);
				if (p != null) {
					ShapeRule nextShapeRule = p.getNestedRule();
					if (nextShapeRule != null) {
						PathStep nextStep = stepList.get(1);
						if (nextStep instanceof DirectionStep) {
							DirectionStep dirStep = (DirectionStep) nextStep;
							if (dirStep.getDirection().equals(Direction.OUT)) {
								PathTerm term = dirStep.getTerm();
								if (term instanceof IriValue) {
									value = (IriValue) term;
									predicate = value.getIri();
									p = nextShapeRule.getProperty(predicate);
									if (p != null) {
										channel = p.getDataChannel();
										if (channel != null) {
											TableItemExpression tableItem = tableMap.tableForVariable(channel.getName());
											if (tableItem instanceof TableAliasExpression) {
												TableAliasExpression tableAlias = (TableAliasExpression) tableItem;
												builder.append(tableAlias.getAlias());
											}
										}
									}
								}
							}
						}
					}
					
				}
			}
			return channel;
		}

		private void appendTable(StringBuilder builder, DataChannel channel) {
			if (channel != null) {
				TableItemExpression tableItem = tableMap.tableForVariable(channel.getName());
				if (tableItem instanceof TableAliasExpression) {
					TableAliasExpression alias = (TableAliasExpression) tableItem;
					builder.append(alias.getAlias());
					builder.append('.');
				}
			}
			
		}

		private DataChannel channel(DataChannel channel, PropertyRule propertyRule) {
			if (propertyRule != null) {
				DataChannel newChannel = propertyRule.getDataChannel();
				if (newChannel != null && channel!=null && newChannel != channel) {
					throw new KonigException("Switching channels not supported");
				}
				channel = newChannel;
			}
			return channel;
		}

		private ShapeRule nestedShapeRule(PropertyRule propertyRule) {
			return propertyRule==null ? null : propertyRule.getNestedRule();
		}

		private PropertyRule propertyRule(ShapeRule shapeRule, URI predicate) {
			return shapeRule == null ? null : shapeRule.getProperty(predicate);
		}


		private NumericValueExpression ifFunction(IfFunction primary) throws ShapeTransformException {
			
			BooleanTerm condition = booleanTerm(primary.getCondition());
			ValueExpression whenTrue = valueExpression(primary.getWhenTrue());
			ValueExpression whenFalse = valueExpression(primary.getWhenFalse());
			
			return new IfExpression(condition, whenTrue, whenFalse);
			
		}

		private BooleanTerm booleanTerm(Expression condition) throws ShapeTransformException {
			ValueExpression e = valueExpression(condition);
			if (e instanceof BooleanTerm) {
				return (BooleanTerm) e;
			}
			throw new ShapeTransformException("Unsupported expression type: " + e.getClass().getSimpleName());
		}

		private 
		ValueExpression literal(LiteralFormula primary) {
			Literal literal = primary.getLiteral();
			Object obj = BeanUtil.toJavaObject(literal);
			if (obj instanceof Number) {
				return new SignedNumericLiteral((Number) obj);
			} if (obj instanceof String) {
				return new StringLiteralExpression((String)obj);
			}
			
			throw new KonigException("Unsupported literal: " + literal);
		}
	}

}
