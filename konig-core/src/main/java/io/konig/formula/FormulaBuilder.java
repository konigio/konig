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
import org.openrdf.model.impl.URIImpl;

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
	
	public FormulaBuilder iri(String value) {
		FullyQualifiedIri id = new FullyQualifiedIri(new URIImpl(value));
		formula = QuantifiedExpression.wrap(id);
		return this;
	}

	public QuantifiedExpression getFormula() {
		return formula;
	}
	
	
	public IfFunctionBuilder<FormulaBuilder> beginIf() {
		return new IfFunctionBuilder<FormulaBuilder>(this);
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
	
	public static class BinaryRelationBuilder<T extends FormulaConsumer> {
		private T formulaConsumer;
		private BinaryOperator operator;
		private NumericExpression left;
		private NumericExpression right;
		
		public BinaryRelationBuilder(T formulaConsumer, BinaryOperator operator) {
			this.formulaConsumer = formulaConsumer;
			this.operator = operator;
		}
		
		
		
		public T endEquals() {
			BinaryRelationalExpression binary = new BinaryRelationalExpression(operator, left, right);
			
			formulaConsumer.setFormula(QuantifiedExpression.wrap(binary));
			return formulaConsumer;
		}
		
		public LeftNumericBuilder<BinaryRelationBuilder<T>> beginLeft() {
			return new LeftNumericBuilder<BinaryRelationBuilder<T>>(new NumericExpressionConsumer(){

				@Override
				public void setNumericExpression(NumericExpression numeric) {
					left = numeric;
				}
				
			}, this);
		}
		
		public RightNumericBuilder<BinaryRelationBuilder<T>> beginRight() {
			return new RightNumericBuilder<BinaryRelationBuilder<T>>(new NumericExpressionConsumer(){

				@Override
				public void setNumericExpression(NumericExpression numeric) {
					right = numeric;
				}
				
			}, this);
		}
		
		void setLeft(NumericExpression left) {
			this.left = left;
		}


		void setRight(NumericExpression right) {
			this.right = right;
		}
		
		
		
	}
	
	public static class LeftNumericBuilder<T> extends NumericExpressionBuilder<T> {

		public LeftNumericBuilder(NumericExpressionConsumer numericConsumer, T formulaConsumer) {
			super(numericConsumer, formulaConsumer);
		}
		
		public T endLeft() {
			return end();
		}
		
	}
	
	public static class RightNumericBuilder<T> extends NumericExpressionBuilder<T> {

		public RightNumericBuilder(NumericExpressionConsumer numericConsumer, T formulaConsumer) {
			super(numericConsumer, formulaConsumer);
		}
		
		public T endRight() {
			return end();
		}
		
	}
	
	public static class NumericExpressionBuilder<T> extends FormulaBuilder {
		private NumericExpressionConsumer numericConsumer;
		private T formulaConsumer;
		
		public NumericExpressionBuilder(NumericExpressionConsumer numericConsumer, T formulaConsumer) {
			this.numericConsumer = numericConsumer;
			this.formulaConsumer = formulaConsumer;
		}
		
		
		protected T end() {
			numericConsumer.setNumericExpression(getFormula().asNumericExpression());
			return formulaConsumer;
		}
	}
	
	
	public static class IfFunctionBuilder<T extends FormulaConsumer> {
		private T formulaConsumer;
		private Expression condition;
		private Expression whenTrue;
		private Expression whenFalse;
		
		
		public IfFunctionBuilder(T formulaConsumer) {
			this.formulaConsumer = formulaConsumer;
		}

		public IfConditionBuilder<T> beginCondition() {
			return new IfConditionBuilder<T>(this);
		}
		
		public WhenTrueBuilder<T> beginWhenTrue() {
			return new WhenTrueBuilder<T>(this);
		}
		
		public T endIf() {
			IfFunction func = new IfFunction(condition, whenTrue, whenFalse);
			formulaConsumer.setFormula(QuantifiedExpression.wrap(func));
			return formulaConsumer;
		}
		
		void setFormulaConsumer(T formulaConsumer) {
			this.formulaConsumer = formulaConsumer;
		}
		void setCondition(Expression condition) {
			this.condition = condition;
		}
		void setWhenTrue(Expression whenTrue) {
			this.whenTrue = whenTrue;
		}
		void setWhenFalse(Expression whenFalse) {
			this.whenFalse = whenFalse;
		}
		
		
		
		
	}
	
	public static class IfConditionBuilder<T extends FormulaConsumer> extends FormulaBuilder {
		private IfFunctionBuilder<T> ifFunction;

		public IfConditionBuilder(IfFunctionBuilder<T> ifFunction) {
			this.ifFunction = ifFunction;
		}
		
		public IfFunctionBuilder<T> endCondition() {
			ifFunction.setCondition(getFormula());
			return ifFunction;
		}
		
		public BinaryRelationBuilder<IfConditionBuilder<T>> beginEquals() {
			return new BinaryRelationBuilder<IfConditionBuilder<T>>(this, BinaryOperator.EQUALS);
		}
		
	}
	
	public static class WhenTrueBuilder<T extends FormulaConsumer> extends FormulaBuilder {
		private IfFunctionBuilder<T> ifFunction;

		public WhenTrueBuilder(IfFunctionBuilder<T> ifFunction) {
			this.ifFunction = ifFunction;
		}
		
		public IfFunctionBuilder<T> endWhenTrue() {
			ifFunction.setWhenTrue(getFormula());
			return ifFunction;
		}
		
	}

	
	public static class WhenFalseBuilder<T extends FormulaConsumer> extends FormulaBuilder {
		private IfFunctionBuilder<T> ifFunction;

		public WhenFalseBuilder(IfFunctionBuilder<T> ifFunction) {
			this.ifFunction = ifFunction;
		}
		
		public IfFunctionBuilder<T> endWhenFalse() {
			ifFunction.setWhenFalse(getFormula());
			return ifFunction;
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
