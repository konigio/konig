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

public class NullPredicate extends AbstractExpression implements BooleanPrimary {
	
	private ValueExpression value;
	private boolean isNull;

	public NullPredicate(ValueExpression value, boolean isNull) {
		this.value = value;
		this.isNull = isNull;
	}

	public ValueExpression getValue() {
		return value;
	}

	public boolean isNull() {
		return isNull;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(value);
		out.print(" IS ");
		if (!isNull) {
			out.print("NOT ");
		}
		out.print("NULL");

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "value", value);
		
	}

}
