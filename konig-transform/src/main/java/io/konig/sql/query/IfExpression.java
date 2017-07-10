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

public class IfExpression extends AbstractExpression implements NumericValueExpression {
	
	private BooleanTerm condition;
	private ValueExpression whenTrue;
	private ValueExpression whenFalse;
	

	public IfExpression(BooleanTerm condition, ValueExpression whenTrue, ValueExpression whenFalse) {
		this.condition = condition;
		this.whenTrue = whenTrue;
		this.whenFalse = whenFalse;
	}
	
	public BooleanTerm getCondition() {
		return condition;
	}

	public ValueExpression getWhenTrue() {
		return whenTrue;
	}

	public ValueExpression getWhenFalse() {
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

}
