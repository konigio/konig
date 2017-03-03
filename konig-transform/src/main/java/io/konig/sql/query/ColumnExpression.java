package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class ColumnExpression extends AbstractExpression
implements ItemExpression, PathExpression, ValueExpression {
	
	private String columnName;
	

	public ColumnExpression(String columnName) {
		this.columnName = columnName;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(columnName);
	}

	public String getColumnName() {
		return columnName;
	}
	
	

}
