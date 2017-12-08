package io.konig.transform.proto;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;
import io.konig.core.vocab.KonigTime;
import io.konig.formula.BareExpression;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.FullyQualifiedIri;
import io.konig.formula.FunctionExpression;
import io.konig.formula.LiteralFormula;
import io.konig.formula.PathExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.VariableTerm;
import io.konig.shacl.PropertyConstraint;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FixedValuePropertyRule;
import io.konig.transform.rule.FormulaPropertyRule;
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
					
					Literal literal = new LiteralImpl(timeUnit.name());
					
					nested.addPropertyRule(new FixedValuePropertyRule(null, Konig.durationUnit, literal));
					
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

						BareExpression timeUnitLiteral = BareExpression.wrap(new LiteralFormula(literal));
						FunctionExpression function = new FunctionExpression("DATE_TRUNC", intervalStartArg, timeUnitLiteral);
						
						QuantifiedExpression sourceFormula = QuantifiedExpression.wrap(function);
						PropertyConstraint sourceProperty = new PropertyConstraint(Konig.intervalStart);
						sourceProperty.setFormula(sourceFormula);
						
						
						FormulaPropertyRule newFormulaRule = new FormulaPropertyRule(targetChannel, targetProperty, sourceProperty);
											
						q = newFormulaRule;
					} else {
						throw new ShapeTransformException("Expected intervalStart to be of type FormulaPropertyRule in Shape: " + shapeRule.getTargetShape().getId());
					}
					
				
					
				} else {
					q = propertyRule(shapeRule, p);
					if (q == null) {
						continue;
					}
				}
				
				nextRule.addPropertyRule(q);
			}


			nextRule.setFromItem(targetChannel);

		}
	
	}

	private BareExpression intervalStartArg() {
		PathExpression path = new PathExpression();
		path.add(new DirectionStep(Direction.OUT, new VariableTerm(VARIABLE_NAME)));
		path.add(new DirectionStep(Direction.OUT, new FullyQualifiedIri(Konig.intervalStart)));
		
		return BareExpression.wrap(path);
	}

	private PropertyRule propertyRule(ShapeRule shapeRule, PropertyModel p) throws ShapeTransformException {
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
				return new ExactMatchPropertyRule(targetChannel, predicate);
			}
			
		}
		throw new ShapeTransformException("PropertyModel type not supported: " + p.getClass().getSimpleName());
	}

}
