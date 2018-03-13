package io.konig.sql.query;

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


import io.konig.core.io.PrettyPrintWriter;

public class ComparisonPredicate extends AbstractExpression implements BooleanTerm {

	private ComparisonOperator operator;
	private ValueExpression left;
	private ValueExpression right;
	
	public ComparisonPredicate(ComparisonOperator operator, ValueExpression left, ValueExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}


	public ComparisonOperator getOperator() {
		return operator;
	}


	public ValueExpression getLeft() {
		return left;
	}



	public ValueExpression getRight() {
		return right;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		left.print(out);
		out.print(operator.getText());
		right.print(out);
	}


	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "left", left);
		visit(visitor, "right", right);
		
	}

}
