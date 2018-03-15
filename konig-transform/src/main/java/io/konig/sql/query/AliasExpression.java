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

public class AliasExpression extends AbstractExpression 
implements ValueExpression, GroupingElement {
	private QueryExpression expression;
	private String alias;

	
	public AliasExpression(QueryExpression expression, String alias) {
		this.expression = expression;
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}


	public QueryExpression getExpression() {
		return expression;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		expression.print(out);
		out.append(" AS ");
		out.append(alias);
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "expression", expression);
	}


}
