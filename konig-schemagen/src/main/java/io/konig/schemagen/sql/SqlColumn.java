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


import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class SqlColumn extends AbstractPrettyPrintable {
	private String columnName;
	private FacetedSqlDatatype datatype;
	private SqlKeyType keytype;
	private boolean nullable;
	
	public SqlColumn(String columnName, FacetedSqlDatatype datatype, SqlKeyType keytype, boolean nullable) {
		this.columnName = columnName;
		this.datatype = datatype;
		this.keytype = keytype;
		this.nullable = nullable;
	}

	public String getColumnName() {
		return columnName;
	}

	public FacetedSqlDatatype getDatatype() {
		return datatype;
	}

	public SqlKeyType getKeytype() {
		return keytype;
	}

	public boolean isNullable() {
		return nullable;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(columnName);
		out.print(' ');
		datatype.print(out);
		if (!nullable) {
			out.print(" NOT NULL");
		}
	}

}
