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
	
	private TableItemExpression leftTable;
	private TableItemExpression rightTable;
	private OnExpression joinSpecification;

	public JoinExpression(TableItemExpression leftTable, TableItemExpression rightTable,
			OnExpression joinSpecification) {
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.joinSpecification = joinSpecification;
	}
	
	public TableItemExpression getLeftTable() {
		return leftTable;
	}

	public TableItemExpression getRightTable() {
		return rightTable;
	}

	public OnExpression getJoinSpecification() {
		return joinSpecification;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println();
		out.pushIndent();
		out.indent();
		leftTable.print(out);
		
		if (joinSpecification!=null) {
			out.println();
			out.popIndent();
			out.indent();

			out.println(" JOIN");

			if (rightTable instanceof JoinExpression) {
				rightTable.print(out);
			} else {
				out.pushIndent();
				out.indent();
				rightTable.print(out);
				out.println();
				out.popIndent();
			}
			joinSpecification.print(out);
		} else {
			out.print(',');
			if (!(rightTable instanceof JoinExpression)) {
				out.println();
				out.indent();
			}
			out.popIndent();
			rightTable.print(out);
		}
		
		
	}


}
