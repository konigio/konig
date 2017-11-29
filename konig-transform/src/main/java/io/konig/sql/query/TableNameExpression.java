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

public class TableNameExpression extends AbstractExpression implements TableItemExpression {
	
	private String tableName;
	private boolean withQuotes;
	public TableNameExpression(String tableName) {
		this.tableName = tableName;
	}
	
	public TableNameExpression(String tableName, boolean withQuotes) {
		this.tableName = tableName;
		this.withQuotes = withQuotes;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		if (withQuotes) {
			out.print('`');
		}
		out.print(tableName);
		if (withQuotes) {
			out.print('`');
		}
		
	}
	public String getTableName() {
		return tableName;
	}
	public boolean isWithQuotes() {
		return withQuotes;
	}
	public void setWithQuotes(boolean withQuotes) {
		this.withQuotes = withQuotes;
	}
	
	

}
