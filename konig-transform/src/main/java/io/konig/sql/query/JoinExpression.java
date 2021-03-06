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


import io.konig.core.io.PrettyPrintWriter;

public class JoinExpression extends AbstractExpression implements TableItemExpression {
	
	private TableItemExpression table;
	private OnExpression joinSpecification;

	public JoinExpression(TableItemExpression table,
			OnExpression joinSpecification) {
		this.table = table;
		this.joinSpecification = joinSpecification;
	}
	

	public TableItemExpression getTable() {
		return table;
	}

	public OnExpression getJoinSpecification() {
		return joinSpecification;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println();
		out.print("JOIN ");
		table.print(out);
		
		if (joinSpecification!=null) {
			out.println();
			out.pushIndent();
			out.indent();
			joinSpecification.print(out);
			out.popIndent();
		} 
		
		
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "table", table);
		visit(visitor, "joinSpecification", joinSpecification);
		
	}


}
