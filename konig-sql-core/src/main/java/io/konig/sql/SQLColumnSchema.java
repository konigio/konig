package io.konig.sql;

public class SQLColumnSchema {
	
	private SQLTableSchema columnTable;
	private String columnName;
	private SQLColumnType columnType;
	private SQLConstraint notNull;
	private SQLConstraint primaryKey;

	public SQLColumnSchema() {
		
	}

	public SQLColumnSchema(SQLTableSchema columnTable, String columnName, SQLColumnType columnType) {
		this.columnTable = columnTable;
		this.columnName = columnName;
		this.columnType = columnType;
		columnTable.addColumn(this);
	}
	


	public SQLColumnSchema(String columnName) {
		this.columnName = columnName;
	}

	public SQLColumnSchema(String columnName, SQLColumnType columnType) {
		this.columnName = columnName;
		this.columnType = columnType;
	}

	public SQLTableSchema getColumnTable() {
		return columnTable;
	}

	public void setColumnTable(SQLTableSchema columnTable) {
		this.columnTable = columnTable;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public SQLColumnType getColumnType() {
		return columnType;
	}

	public void setColumnType(SQLColumnType columnType) {
		this.columnType = columnType;
	}

	public boolean isNotNull() {
		return notNull != null;
	}

	public SQLConstraint getNotNull() {
		return notNull;
	}

	public void setNotNull(SQLConstraint notNull) {
		this.notNull = notNull;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey != null;
	}

	public SQLConstraint getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(SQLConstraint primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getFullName() {
		StringBuilder builder = new StringBuilder();
		SQLSchema schema = columnTable.getSchema();
		String schemaName = schema==null ? "global" : schema.getSchemaName().toLowerCase();
		String tableName = columnTable.getTableName();
		builder.append(schemaName);
		builder.append('.');
		builder.append(tableName);
		builder.append('.');
		builder.append(columnName);
		
		return builder.toString();
	}

}
