package io.konig.transform.showl.sql;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.konig.core.showl.VariableGenerator;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.QueryExpressionVisitor;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.TableAliasExpression;

public class SqlValidator {
	
	public void normalize(SelectExpression select) {
		final Map<String, TableAliasExpression> tableAliasMap = new HashMap<>();
		final List<ColumnExpression> columnList = new ArrayList<>();	
		
		select.dispatch(new QueryExpressionVisitor() {

			@Override
			public void enter(QueryExpression subject) {
				if (subject instanceof TableAliasExpression) {
					TableAliasExpression a = (TableAliasExpression) subject;
					tableAliasMap.put(a.getAlias(), a);
				}
				if (subject instanceof ColumnExpression) {
					columnList.add((ColumnExpression) subject);
				}
				
			}

			@Override
			public void visit(QueryExpression subject, String predicate, QueryExpression object) {
				
			}

			@Override
			public void leave(QueryExpression subject) {
				
			}
			
		});
		
		List<TableAliasExpression> infoList = new ArrayList<>(tableAliasMap.values());
		Collections.sort(infoList, new Comparator<TableAliasExpression>(){

			@Override
			public int compare(TableAliasExpression a, TableAliasExpression b) {
				
				return a.getTableName().toString().compareTo(b.getTableName().toString());
			}});
		

		VariableGenerator vargen = new VariableGenerator();
		for (TableAliasExpression alias : infoList) {
			
			String newAlias = vargen.next();
			alias.setAlias(newAlias);
			
		}
		
		for (ColumnExpression c : columnList) {
			String name = c.getColumnName();
			int dot = name.indexOf('.');
			if (dot > 0) {
				String tableAlias = name.substring(0, dot);
				TableAliasExpression e = tableAliasMap.get(tableAlias);
				if (e != null) {
					String newAlias = e.getAlias();
					name = newAlias + name.substring(dot);
					c.setColumnName(name);
				}
			}
		}
		
		
	}
	
	
}
