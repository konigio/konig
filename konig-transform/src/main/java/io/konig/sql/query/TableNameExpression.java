package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class TableNameExpression extends AbstractExpression implements TableItemExpression {
	
	private String tableName;
	public TableNameExpression(String tableName) {
		this.tableName = tableName;
	}
	@Override
	public void print(PrettyPrintWriter out) {
		out.print(tableName);
		
	}
	public String getTableName() {
		return tableName;
	}
	
	

}
