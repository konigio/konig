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


import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class SelectExpression extends BaseValueContainer implements ValueContainer, QueryExpression, DmlExpression {
	
	
	private FromExpression from = new FromExpression();
	private WhereClause where;
	private GroupByClause groupBy;
	
	public FromExpression getFrom() {
		return from;
	}

	public WhereClause getWhere() {
		return where;
	}

	public void setWhere(WhereClause where) {
		this.where = where;
	}

	public GroupByClause getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(GroupByClause groupBy) {
		this.groupBy = groupBy;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("SELECT");
		List<ValueExpression> values = getValues();
		if (inline()) {
			out.print(' ');
			out.print(values.get(0));
		} else {
			out.pushIndent();
			String comma = "";
			for (ValueExpression value : values) {
				out.print(comma);
				out.println();
				out.indent();
				value.print(out);
				comma = ",";
			}
			out.popIndent();
		}
		
		out.println();
		if (from != null) {
			out.indent();
			from.print(out);
		}
		if (where != null) {
			out.println();
			out.indent();
			out.print(where);
		}
		if (groupBy != null) {
			out.println();
			out.indent();
			out.print(groupBy);
		}
		
	}

	private boolean inline() {

		List<ValueExpression> values = getValues();
		if (values.size()==1) {
			ValueExpression e = values.get(0);
			if (e instanceof ColumnExpression) {
				return true;
			}
		}
		
		return false;
	}
	

}
