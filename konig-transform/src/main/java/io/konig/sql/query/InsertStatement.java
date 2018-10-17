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

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class InsertStatement extends AbstractExpression implements DmlExpression {

	private static final int MAX_WIDTH = 100;
	private TableNameExpression targetTable;
	private List<ColumnExpression> columns;
	private SelectExpression selectQuery;
	private UpdateExpression update;

	public InsertStatement(TableNameExpression tableName, List<ColumnExpression> columns, SelectExpression selectQuery) {
		this.targetTable = tableName;
		this.columns = columns;
		this.selectQuery = selectQuery;
	}

	public TableNameExpression getTargetTable() {
		return targetTable;
	}

	public List<ColumnExpression> getColumns() {
		return columns;
	}

	public SelectExpression getSelectQuery() {
		return selectQuery;
	}


	@Override
	public void print(PrettyPrintWriter out) {

		out.print("INSERT INTO ");
		out.print(targetTable);
		out.print(" (");
		out.pushIndent();
		int width = 0;
		String comma = "";
		for (ColumnExpression c : columns) {
			out.print(comma);
			comma = ", ";
			String name = c.getColumnName();
			width += name.length() + 2;
			if (width > MAX_WIDTH) {
				out.println();
				out.indent();
				width = name.length();
			}
			out.print(c);
		}
		out.println(')');
		out.popIndent();
		out.indent();
		out.print(selectQuery);
	
		if (update != null && !update.getItemList().isEmpty()) {
			out.println();
			out.indent();
			out.print("ON DUPLICATE KEY ");
			out.print(update);
		}

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		for (ColumnExpression column : columns) {
			visit(visitor, "column", column);
		}
		visit(visitor, "selectQuery", selectQuery);
		visit(visitor, "targetTable", targetTable);
	}

	private String getSourceCoulmn(SelectExpression selectQuery, int i) {
		String sourceColumn = "";
		sourceColumn = selectQuery.getValues().get(i).toString();
		if (sourceColumn.contains("AS")) {
			int index = sourceColumn.indexOf(' ');
			sourceColumn = sourceColumn.substring(0, index);
		}
		return sourceColumn;
	}

	public PrettyPrintWriter upsert(PrettyPrintWriter out) {
		out.print("INSERT INTO ");
		out.print(targetTable);
		out.print(" (");
		out.pushIndent();
		int width = 0;
		String comma = "";
		for (ColumnExpression c : columns) {
			String name = c.getColumnName();
			if (name.contains("id")) {

			} else {
				out.print(comma);
				comma = ", ";
				width += name.length() + 2;
				if (width > MAX_WIDTH) {
					out.println();
					out.indent();
					width = name.length();
				}
				out.print(c);
			}
		}
		out.println(')');
		out.popIndent();
		out.indent();
		out.print(selectQuery);
		String commStr = "";
		for (int i = 0; i < columns.size(); i++) {
			String name = columns.get(i).getColumnName();
			if (name.contains("id")) {

			} else {
				out.print(commStr);
				commStr = ", ";
				width += name.length();
				if (width > MAX_WIDTH) {
					out.println();
					out.indent();
					width = name.length();
				}
				out.print(name);
				out.print("=");
				String fromTable = selectQuery.getFrom().toString();
				fromTable = fromTable.substring(5);
				String sourceColumn = getSourceCoulmn(selectQuery, i);
				if ((sourceColumn).contains("modified")) {
					out.print(sourceColumn);
				} else {
					out.print(fromTable + "." + sourceColumn);
				}
			}
		}

		return out;

	}

	public UpdateExpression getUpdate() {
		return update;
	}

	public void setUpdate(UpdateExpression update) {
		this.update = update;
	}

}
