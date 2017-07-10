package io.konig.schemagen.maven;

/*
 * #%L
 * Konig Maven Common
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

public class GoogleCloudPlatformConfig {
	

    private String bqShapeBaseURL;
    private File gcpDir;
	private String bigQueryDatasetId;
	private File enumShapeDir;
	private String enumShapeNameTemplate;
    private File credentials;
	private DataServicesConfig dataServices;
	private boolean enableBigQueryTransform=true;
	
	public GoogleCloudPlatformConfig() {
		
	}
	
	public String getBqShapeBaseURL() {
		return bqShapeBaseURL;
	}
	public void setBqShapeBaseURL(String bqShapeBaseURL) {
		this.bqShapeBaseURL = bqShapeBaseURL;
	}
	public File getGcpDir() {
		return gcpDir;
	}
	public void setGcpDir(File gcpDir) {
		this.gcpDir = gcpDir;
	}
	public String getBigQueryDatasetId() {
		return bigQueryDatasetId;
	}
	public void setBigQueryDatasetId(String bigQueryDatasetId) {
		this.bigQueryDatasetId = bigQueryDatasetId;
	}

	public File getEnumShapeDir() {
		return enumShapeDir;
	}

	public void setEnumShapeDir(File enumShapeDir) {
		this.enumShapeDir = enumShapeDir;
	}

	public String getEnumShapeNameTemplate() {
		return enumShapeNameTemplate;
	}

	public void setEnumShapeNameTemplate(String enumShapeNameTemplate) {
		this.enumShapeNameTemplate = enumShapeNameTemplate;
	}

	public File gcpDir(RdfConfig defaults) {
		if (gcpDir == null) {
			File root = defaults.getRootDir();
			if (root != null) {
				gcpDir = new File(root, "gcp");
			}
		}
		return gcpDir;
	}

	public File enumShapeDir(RdfConfig defaults) {
		if (enumShapeDir == null) {
			enumShapeDir = defaults.getShapesDir();
		}
		return enumShapeDir;
	}

	public DataServicesConfig getDataServices() {
		return dataServices;
	}

	public void setDataServices(DataServicesConfig dataServices) {
		this.dataServices = dataServices;
	}

	public File getCredentials() {
		return credentials;
	}

	public void setCredentials(File credentials) {
		this.credentials = credentials;
	}

	public boolean isEnableBigQueryTransform() {
		return enableBigQueryTransform;
	}

	public void setEnableBigQueryTransform(boolean enableBigQueryTransform) {
		this.enableBigQueryTransform = enableBigQueryTransform;
	}
	
}
