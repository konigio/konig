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

public class IfFunction extends AbstractFormula implements BuiltInCall {
	
	private Expression condition;
	private Expression whenTrue;
	private Expression whenFalse;

	public IfFunction(Expression condition, Expression whenTrue, Expression whenFalse) {
		this.condition = condition;
		this.whenTrue = whenTrue;
		this.whenFalse = whenFalse;
	}

	public Expression getCondition() {
		return condition;
	}

	public Expression getWhenTrue() {
		return whenTrue;
	}

	public Expression getWhenFalse() {
		return whenFalse;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("IF(");
		out.print(condition);
		out.print(" , ");
		out.print(whenTrue);
		out.print(" , ");
		out.print(whenFalse);
		out.print(')');
		
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		condition.dispatch(visitor);
		whenTrue.dispatch(visitor);
		whenFalse.dispatch(visitor);
		visitor.exit(this);
		
	}

}
