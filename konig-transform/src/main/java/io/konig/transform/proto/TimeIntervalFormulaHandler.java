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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;
import io.konig.core.vocab.KonigTime;
import io.konig.formula.BareExpression;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.ConditionalAndExpression;
import io.konig.formula.ConditionalOrExpression;
import io.konig.formula.Expression;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.FullyQualifiedIri;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.IriValue;
import io.konig.formula.MultiplicativeExpression;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.UnaryExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.DataChannel;

public class TimeIntervalFormulaHandler implements FormulaHandler {

	private static Logger logger = LoggerFactory.getLogger(TimeIntervalFormulaHandler.class);
	private static final String TIME_INTERVAL = "TIME_INTERVAL";

	private DataChannelFactory dataChannelFactory;
	
	

	public TimeIntervalFormulaHandler(DataChannelFactory dataChannelFactory) {
		this.dataChannelFactory = dataChannelFactory;
	}



	@Override
	public boolean handleFormula(PropertyGroupHandler groupHandler, PropertyModel targetProperty) throws ShapeTransformException {
		
		URI predicate = targetProperty.getPredicate();
		if (predicate.equals(Konig.timeInterval) && targetProperty instanceof DirectPropertyModel) {
			
			DirectPropertyModel direct = (DirectPropertyModel) targetProperty;
			PropertyConstraint pc = direct.getPropertyConstraint();
			QuantifiedExpression formula = pc.getFormula();
			if (formula != null) {
				PrimaryExpression primary = formula.asPrimaryExpression();
				if (primary instanceof FunctionExpression) {
					FunctionExpression function = (FunctionExpression) primary;
					if (function.getFunctionName().equals(TIME_INTERVAL)) {
						List<Expression> argList = function.getArgList();
						List<KonigTime> durationUnitList = durationUnitList(argList);
						
						Collections.sort(durationUnitList);
						
						KonigTime minTime = durationUnitList.get(0);
						
						Expression intervalStartExpression = argList.get(0);
						
						FunctionExpression intervalStartValue = new FunctionExpression(FunctionModel.DATE_TRUNC, intervalStartExpression, asExpression(minTime));
						
						ShapeModel timeIntervalModel = targetProperty.getValueModel();
						if (timeIntervalModel == null) {
							throw new ShapeTransformException("ShapeModel is not defined for timeInterval");
						}
						
						
						
						PropertyModel intervalStart = timeIntervalModel.getPropertyByPredicate(Konig.intervalStart);
						if (intervalStart == null) {
							throw new ShapeTransformException("intervalStart property is not defined");
						}
						PropertyGroup group = intervalStart.getGroup();
						FormulaPropertyModel value = new FormulaPropertyModel(Konig.intervalStart, group, intervalStartValue);
						group.setSourceProperty(value);
						groupHandler.declareMatch(group);
						value.setDeclaringShape(timeIntervalModel);

						ShapeModel root = timeIntervalModel.rootTargetShapeModel();
						root.addGroupBy(value);
						
						
						PropertyModel durationUnit = timeIntervalModel.getPropertyByPredicate(Konig.durationUnit);
						if (durationUnit == null) {
							throw new ShapeTransformException("durationUnit property is not defined");
						}
						group = durationUnit.getGroup();
						FixedPropertyModel fixed = new FixedPropertyModel(Konig.durationUnit, group, minTime.getIri());
						group.setSourceProperty(fixed);
						groupHandler.declareMatch(group);
						
						if (logger.isDebugEnabled()) {
							logger.debug("handled {}", targetProperty.simplePath());
						}
						
						if (durationUnitList.size()>1) {
							DataChannel channel = dataChannelFactory.createDataChannel(root.getShape());
							root.addPostProcessor(new TimeIntervalRollupProcessor(channel, durationUnitList));
						}
						
						return true;
					}
				}
			}
		}
		return false;
	}


	
	private Expression asExpression(KonigTime minTime) {
		
		IriValue iriValue = new FullyQualifiedIri(minTime.getIri());
		UnaryExpression unary = new UnaryExpression(iriValue);
		MultiplicativeExpression mult = new MultiplicativeExpression(unary);
		GeneralAdditiveExpression add = new GeneralAdditiveExpression(mult);
		BinaryRelationalExpression binary = new BinaryRelationalExpression(null, add, null);
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(binary);
		ConditionalOrExpression or = new ConditionalOrExpression();
		or.add(and);
		BareExpression bare = new BareExpression(or);
		
		return bare;
	}


	private List<KonigTime> durationUnitList(List<Expression> argList) throws ShapeTransformException {
		List<KonigTime> result = new ArrayList<>();
		MyVisitor visitor = new MyVisitor();
		for (int i=1; i<argList.size(); i++) {
			Expression e = argList.get(i);
			visitor.timeValue = null;
			e.dispatch(visitor);
			if (visitor.timeValue==null) {
				throw new ShapeTransformException("Failed to find time unit in formula: " + e.getText());
			}
			result.add(visitor.timeValue);
		}
		return result;
	}
	
	private static class MyVisitor implements FormulaVisitor{
		private KonigTime timeValue=null;

		@Override
		public void enter(Formula formula) {
			
			if (formula instanceof IriValue) {
				IriValue value = (IriValue) formula;
				URI iri = value.getIri();
				timeValue = KonigTime.fromIri(iri);
			}
			
		}

		@Override
		public void exit(Formula formula) {
			
		}
		
		
	}
	

}
