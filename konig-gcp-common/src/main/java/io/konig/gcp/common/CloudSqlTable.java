package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Common
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




public class CloudSqlTable extends com.google.api.client.json.GenericJson{
	  @com.google.api.client.util.Key
	  private String selfLink;
	  @com.google.api.client.util.Key
	  private String instance;
	  @com.google.api.client.util.Key
	  private String database;
	  @com.google.api.client.util.Key
	  private String ddlFile;
	  @com.google.api.client.util.Key
	  private String name;
	  @com.google.api.client.util.Key
	  private String instanceFile;
	  
	  public String getInstanceFile() {
		return instanceFile;
	}
	public void setInstanceFile(String instanceFile) {
		this.instanceFile = instanceFile;
	}
	public String getSelfLink() {
		return selfLink;
	}
	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
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
	public String getDdlFile() {
		return ddlFile;
	}
	public void setDdlFile(String ddlFile) {
		this.ddlFile = ddlFile;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	  
}
