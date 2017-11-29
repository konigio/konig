package io.konig.maven;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import java.io.File;
import java.io.IOException;

import io.konig.gcp.common.GoogleCloudService;
import io.konig.gcp.common.InvalidGoogleCredentialsException;

public class KonigDeployment {
	
	
	private GoogleCloudService service;
	private File baseDir;
	private String response;
	
	public KonigDeployment(String credentials, String baseDir) throws InvalidGoogleCredentialsException, IOException {
		File credentialsFile = new File(credentials);
		service = new GoogleCloudService();
		service.openCredentials(credentialsFile);
		this.baseDir = new File(baseDir).getAbsoluteFile();
	}
	
	public Object insert(InsertResourceType type) {
		switch (type) {
		case BigQueryData:
			return new InsertBigQueryDataAction(this);
		}
		return null;
	}
	
	public Object create(ResourceType type) {
		
		switch (type) {
		case BigQueryDataset :
			return new CreateDatasetAction(this);
			
		case BigQueryTable:
			return new CreateBigqueryTableAction(this);
		
		case BigQueryView:
			return new CreateBigqueryViewAction(this);
			
		case GooglePubSubTopic :
			return new CreateGooglePubSubTopicAction(this);
			
		default:
			break;
			
		}
		return null;
	}
	
	public Object delete(ResourceType type) {
		switch (type) {
		case BigQueryDataset :
			return new DeleteDatasetAction(this);
			
		case BigQueryTable:
			return new DeleteBigqueryTableAction(this);
			
		case BigQueryView:
			return new DeleteBigqueryViewAction(this);
			
		case GooglePubSubTopic :
			return new DeleteGooglePubSubTopicAction(this);
			
		default:
			break;
			
		}
		return null;
	}
	
	public GoogleCloudService getService() {
		return service;
	}
	
	
	public File file(String path) {
		return new File(baseDir, path);
	}
	
	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponse() {
		return this.response;
	}
}
