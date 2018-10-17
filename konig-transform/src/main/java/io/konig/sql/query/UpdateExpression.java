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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class UpdateExpression extends AbstractExpression implements DmlExpression {
	
	private TableItemExpression table;
	private List<UpdateItem> itemList = new ArrayList<>();
	private FromExpression from = new FromExpression();
	private SearchCondition where;
	
	public TableItemExpression getTable() {
		return table;
	}

	public void setTable(TableItemExpression table) {
		this.table = table;
	}

	public FromExpression getFrom() {
		return from;
	}

	public void setFrom(FromExpression from) {
		this.from = from;
	}

	public SearchCondition getWhere() {
		return where;
	}

	public void setWhere(SearchCondition where) {
		this.where = where;
	}

	public void add(UpdateItem item) {
		itemList.add(item);
	}
	
	public List<UpdateItem> getItemList() {
		return itemList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("UPDATE ");
		if (table != null) {
			table.print(out);
		} else {
			out.pushIndent();
		}
		out.println();
		out.indent();
		out.print("SET");
		if (itemList.size()==1) {
			out.print(' ');
			itemList.get(0).print(out);
		} else {
			out.pushIndent();
			String comma = "";
			for (UpdateItem item : itemList) {
				out.println(comma);
				comma = ",";
				out.indent();
				item.print(out);
			}
			out.popIndent();
		}
		if (table ==null) {
			out.popIndent();
		}
		if (!from.getTableItems().isEmpty()) {
			out.println();
			out.indent();
			from.print(out);
		}
		if (where != null) {
			out.println();
			out.indent();
			out.print("WHERE ");
			where.print(out);
		}

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "from", from);
		if (itemList != null) {
			for (UpdateItem item : itemList) {
				visit(visitor, "item", item);
			}
		}
		visit(visitor, "table", table);
		visit(visitor, "where", where);
		
	}

}
