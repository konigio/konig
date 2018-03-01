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

public class CastSpecification extends AbstractExpression implements ValueExpression {

	private ValueExpression value;
	private String datatype;

	public CastSpecification(ValueExpression value, String datatype) {
		this.value = value;
		this.datatype = datatype;
	}

	public ValueExpression getValue() {
		return value;
	}

	public String getDatatype() {
		return datatype;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("CAST(");
		out.print(value);
		out.print(" AS ");
		out.print(datatype);
		out.print(')');
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "value", value);
		
	}

}
