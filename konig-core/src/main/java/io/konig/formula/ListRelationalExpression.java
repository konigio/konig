package io.konig.formula;

/*
 * #%L
 * Konig Core
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


import io.konig.core.io.PrettyPrintWriter;

public class ListRelationalExpression extends AbstractFormula implements RelationalExpression {
	
	private ContainmentOperator operator;
	private NumericExpression left;
	private ExpressionList right;
	
	

	public ListRelationalExpression(ContainmentOperator operator, NumericExpression left, ExpressionList right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public ListRelationalExpression clone() {
		return new ListRelationalExpression(operator, left.clone(), right.clone());
	}
	
	public ContainmentOperator getOperator() {
		return operator;
	}

	public NumericExpression getLeft() {
		return left;
	}

	public ExpressionList getRight() {
		return right;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		left.print(out);
		operator.print(out);
		right.print(out);

	}

	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		operator.dispatch(visitor);
		left.dispatch(visitor);
		right.dispatch(visitor);
		visitor.exit(this);
		
	}

}
