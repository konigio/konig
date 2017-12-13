package io.konig.transform.proto;

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
import org.openrdf.model.impl.LiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;
import io.konig.core.vocab.KonigTime;
import io.konig.formula.BareExpression;
import io.konig.formula.BinaryOperator;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.BuiltInName;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.FullyQualifiedIri;
import io.konig.formula.FunctionExpression;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.LiteralFormula;
import io.konig.formula.NumericExpression;
import io.konig.formula.PathExpression;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.VariableTerm;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.ColumnExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.AnyValuePropertyRule;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FixedValuePropertyRule;
import io.konig.transform.rule.FormulaPropertyRule;
import io.konig.transform.rule.FunctionGroupingElement;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformPostProcessor;

public class TimeIntervalRollupProcessor  implements TransformPostProcessor {
	private static final String VARIABLE_NAME = "?x";
	private static final Logger logger = LoggerFactory.getLogger(TimeIntervalRollupProcessor.class);
	private DataChannel targetChannel;
	private List<KonigTime> timeUnits;
	
	

	public TimeIntervalRollupProcessor(DataChannel targetChannel, List<KonigTime> timeUnits) {
		this.targetChannel = targetChannel;
		this.timeUnits = timeUnits;
	}

	@Override
	public void process(ShapeRule shapeRule) throws ShapeTransformException {
		ShapeModel shapeModel = shapeRule.getTargetShapeModel();
		
		FunctionExpression function = null;
		
		ShapeRule priorRule = shapeRule;
		for (int i = 1; i < timeUnits.size(); i++) {
			KonigTime timeUnit = timeUnits.get(i);

			ShapeRule nextRule = new ShapeRule(shapeModel);
			priorRule.setRollUp(nextRule);
			priorRule = nextRule;
			
			for (PropertyModel p : shapeModel.getProperties()) {
				
				URI predicate = p.getPredicate();
				if (logger.isDebugEnabled()) {
					logger.debug("process property: " + predicate.getLocalName());
				}
				PropertyRule q = null;
				
				if (Konig.timeInterval.equals(predicate)) {
					ContainerPropertyRule container = new ContainerPropertyRule(predicate);
					container.setContainer(nextRule);
					ShapeRule nested = new ShapeRule(p.getValueModel());
					container.setNestedRule(nested);
					
					q = container;
					
					
					PropertyRule timeInterval = shapeRule.getProperty(Konig.timeInterval);
					if (timeInterval == null) {
						throw new ShapeTransformException("timeInterval property not found in shape: " + shapeRule.getTargetShape().getId());
					}
					PropertyRule intervalStart = timeInterval.getNestedRule().getProperty(Konig.intervalStart);
					if (intervalStart == null) {
						throw new ShapeTransformException("intervalStart property not found in shape: " + shapeRule.getTargetShape().getId());
					}
					
					if (intervalStart instanceof FormulaPropertyRule) {
						FormulaPropertyRule formulaRule = (FormulaPropertyRule) intervalStart;
						PropertyConstraint targetProperty = formulaRule.getTargetProperty();
						
						BareExpression intervalStartArg = intervalStartArg();

						BareExpression timeUnitLiteral = BareExpression.wrap(new BuiltInName(timeUnit.getIri()));
						function = new FunctionExpression("DATE_TRUNC", intervalStartArg, timeUnitLiteral);
						
						QuantifiedExpression sourceFormula = QuantifiedExpression.wrap(function);
						PropertyConstraint sourceProperty = new PropertyConstraint(Konig.intervalStart);
						sourceProperty.setMaxCount(1);
						sourceProperty.setMinCount(1);
						sourceProperty.setFormula(sourceFormula);
						
						FormulaPropertyRule newFormulaRule = new FormulaPropertyRule(targetChannel, targetProperty, sourceProperty);
											
						ContainerPropertyRule containerRule = new ContainerPropertyRule(Konig.timeInterval);
						ShapeRule timeIntervalRule = new ShapeRule(p.getValueModel());
						containerRule.setNestedRule(timeIntervalRule);

						Literal literal = new LiteralImpl(timeUnit.getIri().getLocalName());
						
						timeIntervalRule.addPropertyRule(new FixedValuePropertyRule(null, Konig.durationUnit, literal));
						timeIntervalRule.addPropertyRule(newFormulaRule);
						
						
						
						q = new AnyValuePropertyRule(containerRule);
					} else {
						throw new ShapeTransformException("Expected intervalStart to be of type FormulaPropertyRule in Shape: " + shapeRule.getTargetShape().getId());
					}
					
				
					
				} else {
					q = propertyRule(nextRule, shapeRule, p);
					if (q == null) {
						continue;
					}
				}
				
				nextRule.addPropertyRule(q);
			}


			nextRule.setFromItem(targetChannel);
			targetChannel.setVariableName(VARIABLE_NAME);
			addGroupingElement(nextRule, function);
			addWhereExpression(nextRule, timeUnits.get(i-1));

		}
	
	}

	private void addWhereExpression(ShapeRule nextRule, KonigTime timeUnit) {
		LiteralFormula literal = new LiteralFormula(new LiteralImpl(timeUnit.getIri().getLocalName()));
		NumericExpression right = GeneralAdditiveExpression.wrap(literal);
		
		PrimaryExpression primary = PathExpression.builder().out(Konig.timeInterval).out(Konig.durationUnit).build();
		NumericExpression left = GeneralAdditiveExpression.wrap(primary);
		
		BinaryRelationalExpression binary = new BinaryRelationalExpression(BinaryOperator.EQUALS, left, right);
		
		nextRule.addWhereExpression(binary);
		
	}

	private void addGroupingElement(ShapeRule nextRule, FunctionExpression dateTrunc) {
		nextRule.addGroupingElement(new FunctionGroupingElement(dateTrunc));
		
	}

	private BareExpression intervalStartArg() {
		PathExpression path = new PathExpression();
		path.add(new DirectionStep(Direction.OUT, new VariableTerm(VARIABLE_NAME)));
		path.add(new DirectionStep(Direction.OUT, new FullyQualifiedIri(Konig.timeInterval)));
		path.add(new DirectionStep(Direction.OUT, new FullyQualifiedIri(Konig.intervalStart)));
		
		return BareExpression.wrap(path);
	}

	private PropertyRule propertyRule(ShapeRule nextRule, ShapeRule shapeRule, PropertyModel p) throws ShapeTransformException {
		PropertyModel measure = null;
		
		if (p instanceof VariablePropertyModel) {
			return null;
			
		} else if (p instanceof BasicPropertyModel) {
			BasicPropertyModel basic = (BasicPropertyModel) p;
			PropertyConstraint c = basic.getPropertyConstraint();
			URI predicate = c.getPredicate();
			if (predicate == null) {
				throw new ShapeTransformException("Cannot handle null predicate");
			}
			if (Konig.measure.equals(c.getStereotype())) {
				if (measure == null) {
					PropertyRule priorRule = shapeRule.getProperty(predicate);
					if (priorRule instanceof FormulaPropertyRule) {
						FormulaPropertyRule formulaRule = (FormulaPropertyRule) priorRule;
						return formulaRule.clone();
					} 
				}
			} else {
				ExactMatchPropertyRule exact = new ExactMatchPropertyRule(targetChannel, predicate);
				
				
				if (Konig.dimension.equals(c.getStereotype())) {
					
					if (c.getShape()==null) {
						shapeRule.addGroupingElement(new ColumnExpression(c.getPredicate().getLocalName()));
					} else {
						Shape shape = c.getShape();
						if (shape.getNodeKind()==NodeKind.IRI) {
							StringBuilder builder = new StringBuilder();
							builder.append(predicate.getLocalName());
							builder.append(".id");
							String path = builder.toString();
							nextRule.addGroupingElement(new ColumnExpression(path));
						} else {
							// TODO: Look for inverse functional attribute.
							throw new ShapeTransformException("Don't know how to handle structured dimension");
						}
					}
					
				}
				
				return new AnyValuePropertyRule(exact);
			}
			
		}
		throw new ShapeTransformException("PropertyModel type not supported: " + p.getClass().getSimpleName());
	}

}
