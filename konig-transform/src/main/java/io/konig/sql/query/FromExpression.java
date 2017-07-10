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

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;

public class FromExpression extends AbstractExpression implements QueryExpression {
	
	private List<TableItemExpression> tableItems = new ArrayList<>();
	
	
	
	public List<TableItemExpression> getTableItems() {
		return tableItems;
	}

	public void add(TableItemExpression e) {
		if (e == null) {
			throw new KonigException("TableItem must not be null");
		}
		tableItems.add(e);
	}
	
	public boolean contains(TableItemExpression e) {
		return tableItems.contains(e);
	}
	

	@Override
	public void print(PrettyPrintWriter out) {
	
		out.print("FROM ");
		String comma = "";
		for (TableItemExpression item : tableItems) {
			out.print(comma);
			item.print(out);
			comma = ", ";
		}
		
	}

	
}
