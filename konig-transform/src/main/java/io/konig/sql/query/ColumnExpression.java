package io.konig.sql.query;

public class ColumnExpression extends AbstractExpression
implements ItemExpression, ValueExpression {
	
	private String columnName;
	

	public ColumnExpression(String columnName) {
		this.columnName = columnName;
	}

	@Override
	public void append(StringBuilder builder) {
		builder.append(columnName);
	}

	@Override
	public String getTargetName() {
		return columnName;
	}

	public String getColumnName() {
		return columnName;
	}
	
	

}
