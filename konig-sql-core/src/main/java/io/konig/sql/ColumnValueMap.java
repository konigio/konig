package io.konig.sql;

import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueMap;

public class ColumnValueMap implements ValueMap {
	
	private SQLColumnSchema column;
	private String camelCase;
	

	public ColumnValueMap(SQLColumnSchema column) {
		this.column = column;
	}


	@Override
	public String get(String name) {
		if ("columnName".equals(name)) {
			return column.getColumnName();
		}
		if ("columnNameCamelCase".equals(name)) {
			if (camelCase == null) {
				camelCase = StringUtil.camelCase(column.getColumnName());
			}
			return camelCase;
		}
		return null;
	}

}
