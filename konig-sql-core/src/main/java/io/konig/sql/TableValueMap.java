package io.konig.sql;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

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
		if (name.equals("targetClassLocalName")) {
			URI targetClass = table.getTargetClass();
			if (targetClass != null) {
				return targetClass.getLocalName();
			}
		}
		if (name.equals("targetClassNamespacePrefix")) {
			URI targetClass = table.getTargetClass();
			if (targetClass != null && table.getNamespaceManager()!=null) {
				Namespace ns = table.getNamespaceManager().findByName(targetClass.getNamespace());
				if (ns != null) {
					return ns.getPrefix();
				}
			}
		}
		return null;
	}


}
