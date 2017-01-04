package io.konig.sql;

/*
 * #%L
 * Konig SQL
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class SQLTableNamerImpl implements SQLNamer {

	private String baseNamespace;
	private Map<String, URI> map = new HashMap<>();
	private String aliasNamespace;

	public SQLTableNamerImpl(String baseNamespace, String aliasNamespace) {
		this.baseNamespace = baseNamespace;
		this.aliasNamespace = aliasNamespace;
	}

	public SQLTableNamerImpl(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}

	public void put(String fullName, URI uri) {
		map.put(fullName, uri);
	}
	
	

	public String getAliasNamespace() {
		return aliasNamespace;
	}

	public void setAliasNamespace(String aliasNamespace) {
		this.aliasNamespace = aliasNamespace;
	}

	public String getBaseNamespace() {
		return baseNamespace;
	}

	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}

	public URI schemaId(String schemaName) {
		URI result = map.get(schemaName);
		if (result == null) {
			StringBuilder builder = new StringBuilder();
			appendSchema(builder, schemaName);
			result = new URIImpl(builder.toString());
			map.put(schemaName, result);
		}
		return result;
	}


	private void appendSchema(StringBuilder builder, String schemaName) {
		builder.append(baseNamespace);
		builder.append(schemaName);
		char c = schemaName.charAt(schemaName.length()-1);
		if (c != '/' && c!= '#' && c!= ':') {
			builder.append('/');
		}
	}
	
	private void appendTableShape(StringBuilder builder, String schemaName, String tableName) {
		URI schemaId = map.get(schemaName);
		if (schemaId == null) {
			appendSchema(builder, schemaName);
		} else {
			builder.append(schemaId.stringValue());
		}
		builder.append(tableName);
		builder.append("Shape");
	}
	
	

	@Override
	public URI tableId(SQLTableSchema table) {
		
		String key = table.getFullName();
		URI result = map.get(key);
		
		if (result == null) {

			StringBuilder builder = new StringBuilder();
			appendTableId(table, builder);
			
			result = new URIImpl(builder.toString());
		}
		
		return result;
		
	}
	
	private void appendTableId(SQLTableSchema table, StringBuilder builder) {
		

		URI namespace = schemaId(table.getSchema().getSchemaName().toLowerCase());
		builder.append(namespace);
		builder.append(table.getTableName());
		builder.append("Shape");
	}

	@Override
	public URI rdfPredicate(SQLColumnSchema column) {
		URI id = map.get(column.getFullName());
		if (id != null) {
			return id;
		}
		SQLTableSchema table = column.getColumnTable();
		SQLSchema schema = table.getSchema();
		
		return rdfPredicate(schema.getSchemaName(), table.getTableName(), column.getColumnName());
	}

	@Override
	public URI schemaId(SQLSchema schema) {
		return schemaId(schema.getSchemaName());
	}
	
	

	@Override
	public URI rdfId(String fullSqlName) {
		
		URI result = map.get(fullSqlName);
		if (result != null) {
			return result;
		}
		
		String[] part = fullSqlName.split("[.]");
		switch (part.length) {
		case 1 :
			return schemaId(fullSqlName);
			
		case 2 :
			return tableId(part[0], part[1]);
			
		case 3:
			return rdfPredicate(part[0], part[1], part[2]);
		}
		return null;
	}

	private URI tableId(String schemaName, String tableName) {
		URI result = map.get(schemaName + "." + tableName);
		if (result == null) {
			StringBuilder builder = new StringBuilder();
			appendTableShape(builder, schemaName, tableName);
			result = new URIImpl(builder.toString());
		}
		return result;
	}

	private URI rdfPredicate(String schemaName, String tableName, String columnName) {

		StringBuilder builder = new StringBuilder();
		if (aliasNamespace != null) {
			builder.append(aliasNamespace);
			builder.append(columnName);
			return new URIImpl(builder.toString());
		}
		
		String tableId = tableId(schemaName, tableName).stringValue();
		builder.append(tableId);
		
		
		char c = tableId.charAt(tableId.length()-1);
		if (c != '/' && c != '#' && c!=':') {
			builder.append('#');
		}
		builder.append(columnName);
		
		return new URIImpl(builder.toString());
	}


	


}
