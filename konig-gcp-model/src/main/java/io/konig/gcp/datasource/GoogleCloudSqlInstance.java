package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Google Cloud Platform Model
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.GCP;

public class GoogleCloudSqlInstance {

	private URI id;
	private GoogleCloudSqlBackendType backendType = GoogleCloudSqlBackendType.SECOND_GEN;
	private GoogleCloudSqlInstanceType instanceType = GoogleCloudSqlInstanceType.CLOUD_SQL_INSTANCE;
	private String name;
	private GoogleCloudSqlRegion region;
	private GoogleCloudSqlVersion databaseVersion=GoogleCloudSqlVersion.MYSQL_5_7;
	
	
	
	public URI getId() {
		return id;
	}
	
	public void setId(URI id) {
		this.id = id;
	}

	@RdfProperty(GCP.BACKEND_TYPE)
	public GoogleCloudSqlBackendType getBackendType() {
		return backendType;
	}
	
	public void setBackendType(GoogleCloudSqlBackendType backendType) {
		this.backendType = backendType;
	}

	@RdfProperty(GCP.INSTANCE_TYPE)
	public GoogleCloudSqlInstanceType getInstanceType() {
		return instanceType;
	}
	public void setInstanceType(GoogleCloudSqlInstanceType instanceType) {
		this.instanceType = instanceType;
	}

	@RdfProperty(GCP.NAME)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@RdfProperty(GCP.REGION)
	public GoogleCloudSqlRegion getRegion() {
		return region;
	}
	public void setRegion(GoogleCloudSqlRegion region) {
		this.region = region;
	}

	@RdfProperty(GCP.DATABASE_VERSION)
	public GoogleCloudSqlVersion getDatabaseVersion() {
		return databaseVersion;
	}
	public void setDatabaseVersion(GoogleCloudSqlVersion databaseVersion) {
		this.databaseVersion = databaseVersion;
	}
	
	
}
