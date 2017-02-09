package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class ColumnExpression extends AbstractExpression
implements ItemExpression, ValueExpression {
	
	private String columnName;
	

	public ColumnExpression(String columnName) {
		this.columnName = columnName;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(columnName);
	}

	@Override
	public String getTargetName() {
		return columnName;
	}

	public String getColumnName() {
		return columnName;
	}
	
	

}
