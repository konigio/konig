package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Google Cloud Platform Model
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Konig;
import io.konig.datasource.TableDataSource;

public class GoogleCloudSqlTable  extends TableDataSource {
	private String instance;
	private String database;
	private String tableName;
	private String tabularFieldNamespace;

	public GoogleCloudSqlTable() {
	}

	@Override
	public String getTableIdentifier() {
		return tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}
	
	public void setName(String name) {
		tableName = name;
	}

	@Override
	public void setId(Resource id) {
		super.setId(id);
		if (tableName == null && id instanceof URI) {
			URI uri = (URI) id;
			tableName = uri.getLocalName();
		}
	}

	@Override
	public String getUniqueIdentifier() {
		StringBuilder builder = new StringBuilder();
		builder.append("GoogleCloudSql:");
		builder.append(instance);
		builder.append(':');
		builder.append(database);
		builder.append(':');
		builder.append(tableName);
		return builder.toString();
	}

	@Override
	public String getSqlDialect() {
		
		return "MySQL 5.7";
	}
	@RdfProperty(Konig.TABULAR_FIELD_NAMESPACE)
	public String getTabularFieldNamespace() {
		return tabularFieldNamespace;
	}

	public void setTabularFieldNamespace(String tabularFieldNamespace) {
		this.tabularFieldNamespace = tabularFieldNamespace;
	}

	@Override
	public String getDdlFileName() {
		StringBuilder builder = new StringBuilder();
		builder.append(instance);
		builder.append('.');
		builder.append(database);
		builder.append('.');
		builder.append(tableName);
		builder.append(".sql");
		
		return builder.toString();
	}

	@Override
	public String getQualifiedTableName() {
		StringBuilder builder = new StringBuilder();
		builder.append(database);
		builder.append('.');
		builder.append(tableName);
		return builder.toString();
	}
}
