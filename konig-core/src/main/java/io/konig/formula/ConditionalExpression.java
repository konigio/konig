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

public class ConditionalExpression extends AbstractFormula implements RelationalExpression {
	

	private NumericExpression condition;
	private NumericExpression whenTrue;
	private NumericExpression whenFalse;

	public ConditionalExpression(NumericExpression condition, NumericExpression whenTrue, NumericExpression whenFalse) {
		this.condition = condition;
		this.whenTrue = whenTrue;
		this.whenFalse = whenFalse;
	}

	public NumericExpression getCondition() {
		return condition;
	}

	public NumericExpression getWhenTrue() {
		return whenTrue;
	}

	public NumericExpression getWhenFalse() {
		return whenFalse;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		condition.print(out);
		out.print(" ? ");
		whenTrue.print(out);
		out.print(" : ");
		whenFalse.print(out);

	}

}
