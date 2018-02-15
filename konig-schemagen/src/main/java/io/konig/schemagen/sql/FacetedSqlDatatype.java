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

public class FacetedSqlDatatype extends AbstractPrettyPrintable {

	public static final FacetedSqlDatatype BOOLEAN = new FacetedSqlDatatype(SqlDatatype.BOOLEAN);
	public static final NumericSqlDatatype BIT = new NumericSqlDatatype(SqlDatatype.BIT, true);
	public static final NumericSqlDatatype SIGNED_TINYINT = new NumericSqlDatatype(SqlDatatype.TINYINT, false);
	public static final NumericSqlDatatype UNSIGNED_TINYINT = new NumericSqlDatatype(SqlDatatype.TINYINT, true);
	public static final NumericSqlDatatype SIGNED_SMALLINT = new NumericSqlDatatype(SqlDatatype.SMALLINT, false);
	public static final NumericSqlDatatype UNSIGNED_SMALLINT = new NumericSqlDatatype(SqlDatatype.SMALLINT, true);
	public static final NumericSqlDatatype SIGNED_MEDIUMINT = new NumericSqlDatatype(SqlDatatype.MEDIUMINT, false);
	public static final NumericSqlDatatype UNSIGNED_MEDIUMINT = new NumericSqlDatatype(SqlDatatype.MEDIUMINT, true);
	public static final NumericSqlDatatype SIGNED_INT = new NumericSqlDatatype(SqlDatatype.INT, false);
	public static final NumericSqlDatatype UNSIGNED_INT = new NumericSqlDatatype(SqlDatatype.INT, true);
	public static final NumericSqlDatatype SIGNED_BIGINT = new NumericSqlDatatype(SqlDatatype.BIGINT, false);
	public static final NumericSqlDatatype UNSIGNED_BIGINT = new NumericSqlDatatype(SqlDatatype.BIGINT, true);
	public static final NumericSqlDatatype SIGNED_FLOAT = new NumericSqlDatatype(SqlDatatype.FLOAT, false);
	public static final NumericSqlDatatype UNSIGNED_FLOAT = new NumericSqlDatatype(SqlDatatype.FLOAT, true);
	public static final NumericSqlDatatype SIGNED_DOUBLE = new NumericSqlDatatype(SqlDatatype.DOUBLE, false);
	public static final NumericSqlDatatype UNSIGNED_DOUBLE = new NumericSqlDatatype(SqlDatatype.DOUBLE, true);
	public static final FacetedSqlDatatype DATE = new FacetedSqlDatatype(SqlDatatype.DATE);
	public static final FacetedSqlDatatype DATETIME = new FacetedSqlDatatype(SqlDatatype.DATETIME);
	public static final StringSqlDatatype  IRI = new StringSqlDatatype(SqlDatatype.VARCHAR, 2000);

	public static final NumericSqlDatatype SIGNED_NUMBER =  new NumericSqlDatatype(SqlDatatype.NUMBER, true);
	public static final NumericSqlDatatype UNSIGNED_NUMBER =  new NumericSqlDatatype(SqlDatatype.NUMBER, false);
	public static final FacetedSqlDatatype TIMESTAMP = new FacetedSqlDatatype(SqlDatatype.TIMESTAMP);
	
	
	private SqlDatatype datatype;

	public FacetedSqlDatatype(SqlDatatype datatype) {
		this.datatype = datatype;
	}

	public SqlDatatype getDatatype() {
		return datatype;
	}

	public void setDatatype(SqlDatatype datatype) {
		this.datatype = datatype;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(datatype.name());
		
	}
	
	

}
