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

public class BrackettedExpression extends AbstractFormula implements PrimaryExpression {
	
	private Expression expression;

	public BrackettedExpression(Expression expression) {
		this.expression = expression;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	@Override
	public BrackettedExpression clone() {
		return new BrackettedExpression(expression.clone());
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print('(');
		expression.print(out);
		out.print(')');
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		expression.dispatch(visitor);
		visitor.exit(this);
	}

	
}
