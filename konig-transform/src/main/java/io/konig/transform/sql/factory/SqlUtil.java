package io.konig.transform.sql.factory;

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


import org.openrdf.model.URI;

import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;

public class SqlUtil {

	public static ValueExpression literal(Object value) {
		if (value instanceof String) {
			return new StringLiteralExpression(value.toString());
		}
		if (value instanceof Number) {
			Number number = (Number) value;
			return new SignedNumericLiteral(number);
		}
		
		return null;
	}

	public static ColumnExpression columnExpression(TableItemExpression table, URI predicate) {
		String columnName = columnName(table, predicate);
		return new ColumnExpression(columnName);
	}

	public static String columnName(TableItemExpression table, URI predicate) {
		String name = predicate.getLocalName();
		if (table instanceof TableAliasExpression) {
			TableAliasExpression tableName = (TableAliasExpression) table;
			StringBuilder builder = new StringBuilder();
			builder.append(tableName.getAlias());
			builder.append('.');
			builder.append(name);
			name = builder.toString();
		}
		return name;
	}
}
