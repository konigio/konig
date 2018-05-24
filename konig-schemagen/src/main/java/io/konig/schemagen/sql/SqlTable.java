package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class SqlTable extends AbstractPrettyPrintable {
	
	private String tableName;
	private List<SqlColumn> columnList = new ArrayList<>();

	public SqlTable(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public SqlColumn getColumnByName(String columnName) {
		for (SqlColumn c : columnList) {
			if (c.getColumnName().equals(columnName)) {
				return c;
			}
		}
		return null;
	}
	
	public void addColumn(SqlColumn column) {
		columnList.add(column);
	}

	public List<SqlColumn> getColumnList() {
		return columnList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print(tableName);
		out.print(" (");
		out.pushIndent();
		String comma = "";
		StringBuilder pks = new StringBuilder();
		StringBuilder uks = new StringBuilder();		
		for (SqlColumn column : columnList) {
			out.println(comma);
			comma = ", ";
			out.indent();
			column.print(out);
			if(column.getKeytype() == SqlKeyType.SYNTHETIC_KEY){
				out.print(" NOT NULL AUTO_INCREMENT");
			}
			if (column.getKeytype() == SqlKeyType.PRIMARY_KEY || column.getKeytype() == SqlKeyType.SYNTHETIC_KEY) {				
				if (pks.length() > 0) pks.append(comma);
				pks.append(column.getColumnName());
			}			
			if (column.getKeytype() == SqlKeyType.UNIQUE_KEY) {
				if (uks.length() > 0) uks.append(comma);
				uks.append(column.getColumnName());
			}
		}
		if (pks.length() > 0) {
			out.println(comma);
			out.indent();
			out.print("PRIMARY KEY (" + pks.toString() + ")");
		}
		
		if (uks.length() > 0) {
			out.println(comma);
			out.indent();
			out.print("UNIOUE KEY (" + uks.toString() + ")");
		}	
		
		out.println();
		out.popIndent();
		out.indent();
		out.print(");");
	}
	
	

}
