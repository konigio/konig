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


import java.io.File;

import org.openrdf.model.Resource;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.GCP;

public class GoogleCloudSqlTableInfo  {

	private Resource id;
	private File ddlFile;
	private String tableName;
	private String database;
	private String instance;
	
	private File instanceFile;
	
	public GoogleCloudSqlTableInfo() {
		
	}

	public File getDdlFile() {
		return ddlFile;
	}

	public void setDdlFile(File ddlFile) {
		this.ddlFile = ddlFile;
	}

	public File getInstanceFile() {
		return instanceFile;
	}

	public void setInstanceFile(File instanceFile) {
		this.instanceFile = instanceFile;
	}

	@RdfProperty(GCP.NAME)
	public String getTableName() {
		return tableName;
	}

	@RdfProperty(GCP.NAME)
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public Resource getId() {
		return id;
	}

	public void setId(Resource id) {
		this.id = id;
	}

	
}
