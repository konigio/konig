package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class TableAliasExpression extends AbstractExpression implements TableItemExpression {
	
	private TableItemExpression tableName;
	private String alias;
	
	

	public TableAliasExpression(TableItemExpression tableName, String alias) {
		this.tableName = tableName;
		this.alias = alias;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		tableName.print(out);
		out.print(" AS ");
		out.print(alias);
	}

	public String getAlias() {
		return alias;
	}



	public TableItemExpression getTableName() {
		return tableName;
	}
}
