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
			builder.append(baseNamespace);
			builder.append(schemaName);
			char c = schemaName.charAt(schemaName.length()-1);
			if (c != '/' && c!= '#' && c!= ':') {
				builder.append('/');
			}
			result = new URIImpl(builder.toString());
			map.put(schemaName, result);
		}
		return result;
	}
	

	@Override
	public URI tableId(SQLTableSchema table) {
		StringBuilder builder = new StringBuilder();
		appendTableId(table, builder);
		
		return new URIImpl(builder.toString());
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
		StringBuilder builder = new StringBuilder();
		
		if (aliasNamespace != null) {
			builder.append(aliasNamespace);
			builder.append(column.getColumnName());
			return new URIImpl(builder.toString());
		}
		
		appendTableId(column.getColumnTable(), builder);
		
		char c = builder.charAt(builder.length()-1);
		if (c != '/' && c != '#' && c!=':') {
			builder.append('#');
		}
		builder.append(column.getColumnName());
		
		return new URIImpl(builder.toString());
	}

	@Override
	public URI schemaId(SQLSchema schema) {
		return schemaId(schema.getSchemaName());
	}


}
