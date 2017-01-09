package io.konig.sql;

import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueMap;

public class SchemaValueMap implements ValueMap {
	
	private SQLSchema schema;
	private String pascalCase;

	

	public SchemaValueMap(SQLSchema schema) {
		this.schema = schema;
	}



	@Override
	public String get(String name) {
		if ("schemaName".equals(name)) {
			return schema.getSchemaName();
		}
		if ("schemaNamePascalCase".equals(name)) {
			if (pascalCase == null) {
				pascalCase = StringUtil.PascalCase(schema.getSchemaName());
			}
			return pascalCase;
		}
		return null;
	}

}
