package io.konig.sql;

/*
 * #%L
 * Konig SQL
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


import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

public class XsdSQLDatatypeMapper implements SQLDatatypeMapper {

	

	@Override
	public URI rdfDatatype(SQLDatatype sqlType) {
		switch (sqlType) {
		case BIGINT: return XMLSchema.INTEGER;
		case BINARY: return XMLSchema.BASE64BINARY;
		case BLOB: return XMLSchema.BASE64BINARY;
		case CHAR: return XMLSchema.STRING;
		case CLOB: return XMLSchema.STRING;
		case DATE: return XMLSchema.DATE;
		case DATETIME: return XMLSchema.DATETIME;
		case DECIMAL: return XMLSchema.DECIMAL;
		case DOUBLE: return XMLSchema.DOUBLE;
		case FLOAT: return XMLSchema.FLOAT;
		case IMAGE: return XMLSchema.BASE64BINARY;
		case INT: return XMLSchema.INT;
		case INTEGER: return XMLSchema.INTEGER;
		case NCHAR: return XMLSchema.STRING;
		case NCLOB: return XMLSchema.STRING;
		case NTEXT: return XMLSchema.STRING;
		case NUMERIC: return XMLSchema.DECIMAL;
		case NVARCHAR: return XMLSchema.STRING;
		case REAL: return XMLSchema.DOUBLE;
		case SMALLINT: return XMLSchema.SHORT;
		case TEXT: return XMLSchema.STRING;
		case TIME: return XMLSchema.TIME;
		case TIMESTAMP: return XMLSchema.DATETIME;
		case VARCHAR: return XMLSchema.STRING;
		
		}
		return null;
	}

}
