package io.konig.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SQLSchema {
	
	private String schemaName;
	private Map<String,SQLTableSchema> tableMap = new HashMap<String, SQLTableSchema>();

	public SQLSchema() {
	}

	public SQLSchema(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public void add(SQLTableSchema tableSchema) {
		if (tableSchema.getTableName() == null) {
			throw new SQLSchemaException("Table name must be defined");
		}
		
		tableMap.put(tableSchema.getTableName(), tableSchema);
	}
	
	public SQLTableSchema getTableByName(String tableName) {
		return tableMap.get(tableName);
	}

	public Collection<SQLTableSchema> listTables() {
		return tableMap.values();
	}
}
