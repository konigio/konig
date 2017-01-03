package io.konig.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SQLSchemaManager {

	private Map<String, SQLSchema> schemaMap = new HashMap<>();
	
	public void add(SQLSchema schema) {
		if (schema.getSchemaName() == null) {
			throw new SQLSchemaException("Schema name must be defined");
		}
		
		schemaMap.put(schema.getSchemaName(), schema);
	}
	
	public SQLSchema getSchemaByName(String schemaName) {
		return schemaMap.get(schemaName);
	}
	
	public Collection<SQLSchema> listSchemas() {
		return schemaMap.values();
	}

}
