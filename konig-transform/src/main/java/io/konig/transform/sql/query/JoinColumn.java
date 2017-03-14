package io.konig.transform.sql.query;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.ShapePath;

public class JoinColumn extends JoinElement {

	private String columnName;
	
	public JoinColumn(ShapePath shapePath, TableName tableName, String columnName) {
		super(shapePath, tableName);
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	@Override
	public ValueExpression valueExpression() {
		return getTableName().column(columnName);
	}

	@Override
	protected void printFields(PrettyPrintWriter out) {
		
		out.field("columnName", columnName);
		
	}

}
