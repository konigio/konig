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

public class SimpleWhenClause extends AbstractExpression {
	
	private ValueExpression whenOperand;
	private Result result;
	
	

	public SimpleWhenClause(ValueExpression whenOperand, Result result) {
		if (whenOperand == null) {
			throw new IllegalArgumentException("whenOperand must not be null");
		}
		if (result == null) {
			throw new IllegalArgumentException("result must not be null");
		}
		this.whenOperand = whenOperand;
		this.result = result;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("WHEN ");
		whenOperand.print(out);
		out.print(" THEN ");
		result.print(out);
	}

	public ValueExpression getWhenOperand() {
		return whenOperand;
	}

	public Result getResult() {
		return result;
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "whenOperand", whenOperand);
		visit(visitor, "result", result);
		
	}

	
}
