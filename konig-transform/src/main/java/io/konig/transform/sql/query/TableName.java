package io.konig.transform.sql.query;

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

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;

public class TableName extends AbstractPrettyPrintable {
	private String fullName;
	private String alias;
	private TableItemExpression item;
	
	public TableName(String fullName, String alias) {
		this.fullName = fullName;
		this.alias = alias;
	}

	public String columnName(String name) {
		if (alias == null) {
			return name;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(alias);
		builder.append('.');
		builder.append(name);
		return builder.toString();
	}


	public ColumnExpression column(PropertyConstraint sourceProperty) {
		return column(sourceProperty.getPredicate().getLocalName());
	}
	public ColumnExpression column(URI predicate) {
		return column(predicate.getLocalName());
	}
	
	public ColumnExpression column(String name) {
		return new ColumnExpression(columnName(name));
	}

	public String getFullName() {
		return fullName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public TableNameExpression getExpression() {
		return new TableNameExpression(fullName);
	}

	public TableItemExpression getItem() {
		if (item == null) {
			item = new TableNameExpression(fullName);
			if (alias != null) {
				item = new TableAliasExpression(item, alias);
			}
		}
		return item;
	}

	public void setItem(TableItemExpression item) {
		this.item = item;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("fullName: ");
		out.println(fullName);
		out.indent();
		out.print("alias: ");
		out.println(alias);
		out.indent();
		out.print("item: ");
		out.println(item);
		
	}

	
}
