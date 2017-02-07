package io.konig.datasource;

/*
 * #%L
 * Konig Core
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


import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;

public class GoogleCloudStorageBucket extends DataSource {
	
	private String projectId;
	private String name;
	private String location;
	private String storageClass;

	public GoogleCloudStorageBucket() {
		addType(Konig.GoogleCloudStorageBucket);
	}

	@RdfProperty(GCP.PROJECT_ID)
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	@RdfProperty(GCP.NAME)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@RdfProperty(GCP.LOCATION)
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@RdfProperty(GCP.STORAGE_CLASS)
	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	

	
}
