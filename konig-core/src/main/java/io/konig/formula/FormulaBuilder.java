package io.konig.formula;

/*
 * #%L
 * Konig Core
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
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.shacl.ShapeBuilder.PropertyBuilder;

public class FormulaBuilder implements FormulaConsumer {

	private PropertyBuilder propertyBuilder;
	private QuantifiedExpression formula;
	
	public FormulaBuilder() {
	}

	public PropertyBuilder endFormula() {
		QuantifiedExpression q = getFormula();
		propertyBuilder.getPropertyConstraint().setFormula(q);
		return propertyBuilder;
	}

	public FormulaBuilder(PropertyBuilder propertyBuilder) {
		this.propertyBuilder = propertyBuilder;
	}
	
	public PathBuilder<FormulaBuilder> beginPath() {
		return new PathBuilder<FormulaBuilder>(this);
	}
	
	public FunctionBuilder beginFunction(String functionName) {
		return new FunctionBuilder(this, functionName);
	}

	public QuantifiedExpression getFormula() {
		return formula;
	}

	@Override
	public void setFormula(QuantifiedExpression formula) {
		this.formula = formula;
	}

	private static class DirectedPredicate {
		private Direction direction;
		private URI predicate;
		public DirectedPredicate(Direction direction, URI predicate) {
			this.direction = direction;
			this.predicate = predicate;
		}
		public Direction getDirection() {
			return direction;
		}
		public URI getPredicate() {
			return predicate;
		}
		
		
	}
	
	
	public static class PathBuilder<T extends FormulaConsumer> {

		private T formulaConsumer;
		private List<DirectedPredicate> predicateList;
		
		
		public PathBuilder() {
		}

		public PathBuilder(T formulaConsumer) {
			this.formulaConsumer = formulaConsumer;
		}
		
		

		public PathBuilder<T> out(URI predicate) {
			predicateList().add(new DirectedPredicate(Direction.OUT, predicate));
			return this;
		}
		
		private List<DirectedPredicate> predicateList() {
			if (predicateList == null) {
				predicateList = new ArrayList<>();
			}
			return predicateList;
		}
		
		public PathBuilder<T> in(URI predicate) {
			predicateList().add(new DirectedPredicate(Direction.IN, predicate));
			return this;
		}
		
		public PathBuilder<T> pop() {
			List<DirectedPredicate> list = predicateList();
			list.remove(list.size()-1);
			return this;
		}
		
		public QuantifiedExpression build() {
			PathExpression path = new PathExpression();
			QuantifiedExpression formula = QuantifiedExpression.wrap(path);
			Context context = new BasicContext(null);
			formula.setContext(context);
			for (DirectedPredicate e : predicateList()) {
				URI predicate = e.getPredicate();
				context.add(new Term(predicate.getLocalName(), predicate.stringValue()));
				PathStep step = new DirectionStep(e.getDirection(), new LocalNameTerm(context, predicate.getLocalName()));
				path.add(step);
			}
			return formula;
		}

		
		public T endPath() {
			
			QuantifiedExpression formula = build();
			formulaConsumer.setFormula(formula);
			
			return formulaConsumer;
		}
		
	}
	
	public static class DummyFormulaConsumer implements FormulaConsumer {

		@Override
		public void setFormula(QuantifiedExpression formula) {
			// Do nothing
		}
	}
	
	public static class BasicPathBuilder extends PathBuilder<DummyFormulaConsumer> {
		
	}
	
	
	
	public static class FunctionBuilder implements FormulaConsumer {
		private String functionName;
		private FormulaBuilder formulaBuilder;
		private List<Expression> argList = new ArrayList<>();
		
		public FunctionBuilder(FormulaBuilder formulaBuilder, String functionName) {
			this.formulaBuilder = formulaBuilder;
			this.functionName = functionName;
			
		}
		
		public FunctionBuilder literal(String value) {
			LiteralFormula literal = new LiteralFormula(new LiteralImpl(value));
			Expression e = ConditionalOrExpression.wrap(literal);
			argList.add(e);
			return this;
		}
		
		public PathBuilder<FunctionBuilder> beginPath() {
			return new PathBuilder<FunctionBuilder>(this);
		}
		
		
		public FormulaBuilder endFunction() {
			FunctionExpression function = new FunctionExpression(functionName, argList);
			QuantifiedExpression formula = QuantifiedExpression.wrap(function);
			formulaBuilder.setFormula(formula);
			return formulaBuilder;
		}

		@Override
		public void setFormula(QuantifiedExpression formula) {
			argList.add(formula);
		}
		
		
	}

}
