package io.konig.sql.query;

public class TableAliasExpression extends AbstractExpression implements TableItemExpression {
	
	private TableNameExpression tableName;
	private String alias;
	
	

	public TableAliasExpression(TableNameExpression tableName, String alias) {
		this.tableName = tableName;
		this.alias = alias;
	}



	@Override
	public void append(StringBuilder builder) {
		tableName.append(builder);
		builder.append(" AS ");
		builder.append(alias);
	}

	public String getAlias() {
		return alias;
	}



	public TableNameExpression getTableName() {
		return tableName;
	}
}
