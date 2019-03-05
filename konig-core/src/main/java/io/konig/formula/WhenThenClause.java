package io.konig.formula;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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

public class WhenThenClause extends AbstractFormula {
	
	private Expression when;
	private Expression then;

	public WhenThenClause(Expression when, Expression then) {
		this.when = when;
		this.then = then;
	}

	public Expression getWhen() {
		return when;
	}

	public Expression getThen() {
		return then;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println();
		out.indent();
		out.print("WHEN ");
		when.print(out);
		out.print(" THEN ");
		then.print(out);
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		when.dispatch(visitor);
		then.dispatch(visitor);
		visitor.exit(this);

	}

}
