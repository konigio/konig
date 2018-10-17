package io.konig.sql.query;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


public class SqlExpressionBuilder<T extends SqlExpressionBuilder<?>> implements QueryExpressionConsumer{
	private QueryExpression expression;
	
	public SqlFunctionBuilder<T> beginFunction(String functionName) {
		return new SqlFunctionBuilder<T>(self(), functionName);
	}
	
	@SuppressWarnings("unchecked")
	private T self() {
		return (T) this;
	}
	
	public T column(String columnName) {
		consume(new ColumnExpression(columnName));
		return self();
	}
	
	
	static class SqlFunctionBuilder<T extends QueryExpressionConsumer> {
		T consumer;
		SqlFunctionExpression function;

		public SqlFunctionBuilder(String functionName) {
			this(null, functionName);
		}

		public SqlFunctionBuilder(T consumer, String functionName) {
			this.consumer = consumer;
			this.function = new SqlFunctionExpression(functionName);
		}
		
		public SqlFunctionArgBuilder<T> beginArg() {
			return new SqlFunctionArgBuilder<T>(this);
		}
		
		public T endFunction() {
			consumer.consume(function);
			return consumer;
		}
		
		public SqlFunctionExpression getFunction() {
			return function;
		}
	}
	
	public static class SqlFunctionArgBuilder<T extends QueryExpressionConsumer> extends SqlExpressionBuilder {
		private SqlFunctionBuilder<T> functionBuilder;

		public SqlFunctionArgBuilder(SqlFunctionBuilder<T> functionBuilder) {
			this.functionBuilder = functionBuilder;
		}
		
		public SqlFunctionBuilder<T> endArg() {
			return functionBuilder;
		}
		
		@Override
		public void consume(QueryExpression e) {
			super.consume(e);
			functionBuilder.getFunction().addArg(e);
		}
	}

	@Override
	public void consume(QueryExpression e) {
		expression = e;
	}
	
	public QueryExpression build() {
		return expression;
	}

}
