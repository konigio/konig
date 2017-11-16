package io.konig.transform.sql.factory;

import java.util.ArrayList;
import java.util.List;

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
import io.konig.transform.proto.PropertyModel;
import io.konig.transform.proto.ShapeModel;
import io.konig.transform.rule.DataChannel;

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
	
	public static ColumnExpression columnExpression(PropertyModel pm, TableItemExpression table) {

		if (pm.isTargetProperty()) {
			pm = pm.getGroup().getSourceProperty();
		}
		URI predicate = pm.getPredicate();
		StringBuilder builder = new StringBuilder();
		if (table instanceof TableAliasExpression) {
			TableAliasExpression tableName = (TableAliasExpression) table;
			builder.append(tableName.getAlias());
			builder.append('.');
		}
		
		if (pm!=null && pm.getDeclaringShape().getAccessor()!=null) {
			List<PropertyModel> list = new ArrayList<>();
			
			while (pm != null) {
				list.add(pm);
				ShapeModel sm = pm.getDeclaringShape();
				pm = sm==null ? null : sm.getAccessor();
				
			}
			int last = list.size()-1;
			for (int i=last; i>=0; i--) {
				if (i!=last) {
					builder.append('.');
				}
				pm = list.get(i);
				predicate = pm.getPredicate();
				builder.append(predicate.getLocalName());
			}
		} else {
			String name = predicate.getLocalName();
			builder.append(name);
		}
		String columnName = builder.toString();
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
