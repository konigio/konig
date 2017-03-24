package io.konig.sql.query;

import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class InsertStatement extends AbstractExpression implements DmlExpression {
	
	private static final int MAX_WIDTH = 100;
	private TableNameExpression targetTable;
	private List<ColumnExpression> columns;
	private SelectExpression selectQuery;
	
	public InsertStatement(TableNameExpression tableName, List<ColumnExpression> columns,
			SelectExpression selectQuery) {
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
		
		out.print("INSERT ");
		out.print(targetTable);
		out.print(" (");
		out.pushIndent();
		int width = 0;
		String comma = "";
		for (ColumnExpression c : columns) {
			out.print(comma);
			comma = ", ";
			String name = c.getColumnName();
			width += name.length()+2;
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
	}

}
