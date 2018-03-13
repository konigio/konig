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

public class GroupByClause extends AbstractExpression {
	
	private List<GroupingElement> elementList;

	public GroupByClause(List<GroupingElement> elementList) {
		this.elementList = elementList;
	}
	
	public void add(GroupingElement element) {
		if (elementList == null) {
			elementList = new ArrayList<>();
		}
		elementList.add(element);
	}

	public List<GroupingElement> getElementList() {
		return elementList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("GROUP BY ");
		String comma = "";
		for (GroupingElement e : elementList) {
			out.print(comma);
			comma = ", ";
			out.print(e);
		}
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		for (GroupingElement e : elementList) {
			visit(visitor, "element", e);
		}
		
	}

}
