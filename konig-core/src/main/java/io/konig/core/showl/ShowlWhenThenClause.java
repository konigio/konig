package io.konig.core.showl;

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


import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.formula.Expression;

public class ShowlWhenThenClause implements ShowlExpression {

	private ShowlExpression when;
	private ShowlExpression then;

	public ShowlWhenThenClause(ShowlExpression when, ShowlExpression then) {
		this.when = when;
		this.then = then;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("WHEN ");
		builder.append(when.displayValue());
		builder.append(" THEN ");
		builder.append(then.displayValue());
		return builder.toString();
		
	}
	
	public String toString() {
		return displayValue();
	}

	public ShowlExpression getWhen() {
		return when;
	}

	public ShowlExpression getThen() {
		return then;
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		when.addDeclaredProperties(sourceNodeShape, set);
		then.addDeclaredProperties(sourceNodeShape, set);

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		when.addProperties(set);
		then.addProperties(set);
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return then.valueType(reasoner);
	}

	@Override
	public ShowlWhenThenClause transform() {
		return new ShowlWhenThenClause(when.transform(), then.transform());
	}

}
