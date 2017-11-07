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

import io.konig.core.KonigException;
import io.konig.core.pojo.BeanUtil;
import io.konig.formula.Addend;
import io.konig.formula.AdditiveOperator;
import io.konig.formula.BinaryOperator;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.ConditionalAndExpression;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.FunctionExpression;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.IfFunction;
import io.konig.formula.IriValue;
import io.konig.formula.LiteralFormula;
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
import io.konig.sql.query.IfExpression;
import io.konig.sql.query.NumericValueExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.ShapeRule;

public class SqlFormulaFactory {
	

	public ValueExpression formula(SqlFormulaExchange request) throws TransformBuildException {
		Worker worker = new Worker(request);
		
		return worker.formula(request.getProperty());
	}
	
	private static class Worker {

		private VariableTableMap tableMap;
		private TableItemExpression sourceTable;
		private PropertyConstraint propertyConstraint;
		private SqlFormulaExchange exchange;
		
		public Worker(SqlFormulaExchange request) {
			this.exchange = request;
			this.tableMap = request.getTableMap();
			this.sourceTable = request.getSourceTable();
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
				throw new TransformBuildException("Failed to generate formula for property " +
						p.getPredicate().stringValue(), e
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
				QueryExpression e = pathExpression((PathExpression) primary);
				if (e instanceof NumericValueExpression) {
					return (NumericValueExpression) e;
				}
				throw new KonigException("Expected a NumericValueExpression but found " + e.getClass().getSimpleName());
			}
			
			if (primary instanceof FunctionExpression) {
				FunctionExpression func = (FunctionExpression) primary;
				String funcName = func.getFunctionName();
				io.konig.sql.query.FunctionExpression sqlFunc = new io.konig.sql.query.FunctionExpression(funcName);
				addArguments(sqlFunc, func);
				return sqlFunc;
			}
			
			throw new KonigException("Expression type not supported: " + primary.getClass().getSimpleName());
		}

		private void addArguments(io.konig.sql.query.FunctionExpression sqlFunc, FunctionExpression sparqlFunc) throws ShapeTransformException {
			
			for (Expression e : sparqlFunc.getArgList()) {
				ValueExpression ve = valueExpression(e);
				sqlFunc.addArg(ve);
			}
			
		}
		
		private QueryExpression deepPath(PathExpression primary) {
			try {
				List<PathStep> stepList = primary.getStepList();
				ShapeRule rule = exchange.getShapeRule();
				
				PathStep first = stepList.get(0);
				if (first instanceof DirectionStep && stepList.size()==2) {
					// For now, we only support paths of length 2
					DirectionStep dirStep = (DirectionStep) first;
					if (dirStep.getDirection() == Direction.OUT) {
						PathTerm term = dirStep.getTerm();
						if (term instanceof IriValue) {
							IriValue iriValue = (IriValue) term;
							URI predicate = iriValue.getIri();
							
							PropertyRule propertyRule = rule.getProperty(predicate);
							ShapeRule nested = propertyRule.getNestedRule();
							
							if (nested != null) {
								
								PathStep nextStep = stepList.get(1);
								if (nextStep instanceof DirectionStep) {
									dirStep = (DirectionStep) nextStep;
									if (dirStep.getDirection() == Direction.OUT) {
										term = dirStep.getTerm();
										if (term instanceof IriValue) {
											iriValue = (IriValue) term;
											predicate = iriValue.getIri();
											
											PropertyRule nestedProperty = nested.getProperty(predicate);
											
											

											String tableName = nestedProperty.getDataChannel().getName();
											String localName = predicate.getLocalName();
											
											// TODO: Handle the case where we need an alias instead of 
											// just using the localName!!!!
											
											StringBuilder builder = new StringBuilder();
											builder.append(tableName);
											builder.append('.');
											builder.append(localName);
											
											String columnName = builder.toString();
											
											return new ColumnExpression(columnName);
											
										}
										
									}
								}
								
								
							}
							
							
							
							
						}
					}
				}
			} catch (Throwable e) {
				throw new KonigException(e);
			}
			
			
			
			throw new KonigException("deep path not supported");
		}

		private QueryExpression pathExpression(PathExpression primary) {
			
			List<PathStep> stepList = primary.getStepList();
			if (stepList!=null) {
				// For now, we only support two very limited patterns.
				// (1)  Path consisting of a single OUT Step.
				// (2)  Path consisting of a variable followed by a single OUT step.
				if (stepList.size()==1) {
					PathStep step = stepList.get(0);
					
					if (step instanceof DirectionStep) {
					
						DirectionStep dirStep = (DirectionStep) step;
						if (dirStep.getDirection() == Direction.OUT) {
							PathTerm term = dirStep.getTerm();
							URI predicate = term.getIri();
							
							return SqlUtil.columnExpression(sourceTable, predicate);
						}
					} else {
						return deepPath(primary);
					}
				} else if (stepList.size()==2) {
					PathStep step = stepList.get(0);
					
					if (step instanceof DirectionStep) {
						DirectionStep dirStep = (DirectionStep) step;
						if (dirStep.getDirection() == Direction.OUT) {
							PathTerm term = dirStep.getTerm();
							if (term instanceof VariableTerm) {
								VariableTerm varTerm = (VariableTerm) term;
								String varName = varTerm.getVarName();
								TableItemExpression tableItem = tableMap.tableForVariable(varName);
								if (tableItem == null) {
									throw new KonigException("Table not found for variable in expression: " + primary.toString());
								}
								step = stepList.get(1);
								if (step instanceof DirectionStep) {
									dirStep = (DirectionStep) step;
									if (dirStep.getDirection() == Direction.OUT) {
										term = dirStep.getTerm();
										URI predicate = term.getIri();
										ColumnExpression ge = SqlUtil.columnExpression(tableItem, predicate);
										ValueExpression ve = ge;
										Integer maxCount = propertyConstraint.getMaxCount();
		
										exchange.setGroupingElement(ge);
										if (maxCount == null) {
											io.konig.sql.query.FunctionExpression func = new io.konig.sql.query.FunctionExpression("ARRAY_AGG");
											func.addArg(ve);
											ve = func;
										}
										return ve;
									}
								}
							} else {
								return deepPath(primary);
							}
						}
					} else {
						return deepPath(primary);
					}
				}
			}
			
			throw new KonigException("Unsupported expression: " + primary.toString());
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
