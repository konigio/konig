package io.konig.maven;

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
    
    @Parameter(property="konig.gcp.directory", defaultValue="${project.basedir}/target/generated/gcp")
    private File directory;
    
    
	private String bigQueryDatasetId;
	private File enumShapeDir;
	private String enumShapeNameTemplate;
    private File credentials;
    
    @Parameter(property="konig.gcp.topicsFile", defaultValue="${konig.gcp.directory}/topics.txt")
    private File topicsFile;
    
    @Parameter(property="konig.gcp.dataServices")
	private DataServicesConfig dataServices;
    
	private boolean enableBigQueryTransform=true;
	
	private boolean enableMySqlTransform=true;
	
	@Parameter(property="konig.gcp.bigquery", required=true)
	private BigQueryInfo bigquery;
	
	@Parameter(property="konig.gcp.cloudstorage", required=true)
	private CloudStorageInfo cloudstorage;

	@Parameter(property="konig.gcp.cloudsql", required=true)
	private CloudSqlInfo cloudsql;
	
	@Parameter(property="konig.gcp.deployment")
	private GroovyDeploymentScript deployment;
	
	public GoogleCloudPlatformConfig() {
		
	}
	
	public String getBqShapeBaseURL() {
		return bqShapeBaseURL;
	}
	public void setBqShapeBaseURL(String bqShapeBaseURL) {
		this.bqShapeBaseURL = bqShapeBaseURL;
	}
	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File gcpDir) {
		this.directory = gcpDir;
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
		if (directory == null) {
			File root = defaults.getRootDir();
			if (root != null) {
				directory = new File(root, "gcp");
			}
		}
		return directory;
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
	
	public boolean isEnableMySqlTransform() {
		return enableMySqlTransform;
	}

	public void setEnableMySqlTransform(boolean enableMySqlTransform) {
		this.enableMySqlTransform = enableMySqlTransform;
	}

	public BigQueryInfo getBigquery() {
		return bigquery;
	}

	public void setBigquery(BigQueryInfo bigQuery) {
		this.bigquery = bigQuery;
	}

	public CloudStorageInfo getCloudstorage() {
		return cloudstorage;
	}

	public void setCloudstorage(CloudStorageInfo cloudStorage) {
		this.cloudstorage = cloudStorage;
	}

	public GroovyDeploymentScript getDeployment() {
		return deployment;
	}

	public void setDeployment(GroovyDeploymentScript deployment) {
		this.deployment = deployment;
	}

	public File getTopicsFile() {
		return topicsFile;
	}

	public void setTopicsFile(File topicsFile) {
		this.topicsFile = topicsFile;
	}

	public CloudSqlInfo getCloudsql() {
		return cloudsql;
	}

	public void setCloudsql(CloudSqlInfo cloudsql) {
		this.cloudsql = cloudsql;
	}
	
	
	
}
