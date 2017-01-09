package io.konig.sql;

import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueMap;

public class TableValueMap implements ValueMap {
	
	private SQLTableSchema table;
	private String pascalCase;
	
	

	public TableValueMap(SQLTableSchema table) {
		this.table = table;
	}

	@Override
	public String get(String name) {
		
		if ("tableName".equals(name)) {
			return table.getTableName();
		}
		if ("tableNamePascalCase".equals(name)) {
			if (pascalCase == null) {
				pascalCase = StringUtil.PascalCase(table.getTableName());
			}
			return pascalCase;
		}
		return null;
	}


}
