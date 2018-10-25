package io.konig.gcp.datasource;

import java.text.MessageFormat;

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
import org.openrdf.model.impl.URIImpl;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class GoogleCloudSqlTable  extends TableDataSource {
	private String instance;
	private String database;
	private String tableName;
	private String tabularFieldNamespace;

	public GoogleCloudSqlTable() {
		addType(Konig.GoogleCloudSqlTable);
	}

	public String getTableIdentifier() {
		return tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@RdfProperty(GCP.INSTANCE)
	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	@RdfProperty(GCP.DATABASE)
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	@RdfProperty(GCP.NAME)
	public String getName() {
		return tableName;
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
	public String getTransformFileName() {

		StringBuilder builder = new StringBuilder();
		builder.append(instance);
		builder.append('.');
		builder.append(database);
		builder.append('.');
		builder.append(tableName);
		builder.append(".dml.sql");
		
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
	
	public static class Builder {
		private GoogleCloudSqlTable table = new GoogleCloudSqlTable();
		private ShapeBuilder shapeBuilder;
		
		public Builder(ShapeBuilder shapeBuilder) {
			this.shapeBuilder = shapeBuilder;
		}
		
		public Builder database(String database) {
			table.setDatabase(database);
			return this;
		}
		
		public Builder instance(String instance) {
			table.setInstance(instance);
			return this;
		}
		
		public Builder name(String name) {
			table.setName(name);
			return this;
		}
		
		public Builder id(URI id) {
			table.setId(id);
			return this;
		}
		
		public ShapeBuilder endDataSource() {
			shapeBuilder.peekShape().addShapeDataSource(table);
			return shapeBuilder;
		}
		
		
		
	}

	@Override
	public TableDataSource generateAssociationTable(Shape subjectShape, URI predicate) {
		
		GoogleCloudSqlTable table = new GoogleCloudSqlTable();
		table.setInstance(instance);
		table.setDatabase(database);
		table.setName(associationTableName(subjectShape, predicate));
		table.setTabularFieldNamespace(tabularFieldNamespace);
		

		String pattern = 
			"https://www.googleapis.com/sql/v1beta4/projects/{0}/instances/{1}/databases/{2}/tables/{3}";
		
		String idValue = MessageFormat.format(pattern, "${gcpProjectId}", instance, database, table.getName());
		
		table.setId(new URIImpl(idValue));
		
		return table;
	}
}
