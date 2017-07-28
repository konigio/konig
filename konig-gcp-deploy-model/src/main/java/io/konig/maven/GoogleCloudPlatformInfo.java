package io.konig.maven;

/*
 * #%L
 * Konig Google Cloud Platform Deployment Model
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

import io.konig.yaml.Yaml;

public class GoogleCloudPlatformInfo  {
	
	@Parameter(property="google.credentials")
	private String credentials;
	
	@Parameter(property="konig.deploy.gcp.projectId")
	private String projectId;
	
	@Parameter(property="konig.deploy.gcp.directory")
	private File directory;
	
	@Parameter(property="konig.deploy.gcp.bigquery")
	private BigQueryInfo bigquery = new BigQueryInfo();
	
	@Parameter(property="konig.deploy.gcp.cloudstorage")
	private CloudStorageInfo cloudstorage = new CloudStorageInfo();

	@Parameter(property="konig.deploy.commandFile")
	private File commandFile;

	@Parameter(property="konig.deploy.script")
	private GroovyDeploymentScript deploymentScript;
	
	/**
	 * Get the identifier for the Google Cloud Project.
	 */
	public String getProjectId() {
		return projectId;
	}
	
	/**
	 * Set the identifier for the Google Cloud Project.
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Get the top-level directory in the local file system where Google Cloud resources
	 * are stored.  This directory will typically contain the following sub-directories:
	 * <ul>
	 * 	<li> bigquery/data
	 * 	<li> bigquery/dataset
	 * 	<li> bigquery/schema
	 *  <li> bigquery/scripts
	 * </ul>
	 * @return
	 */
	public File getDirectory() {
		return directory;
	}
	
	

	public BigQueryInfo getBigQuery() {
		return bigquery;
	}

	public void setBigQuery(BigQueryInfo bigQuery) {
		this.bigquery = bigQuery;
	}
	
	

	/**
	 * Specify the location of the top-level directory in the local file system where
	 * Google Cloud Platform resources are stored.
	 * @param gcpDir
	 */
	public void setDirectory(File gcpDir) {
		this.directory = gcpDir;
	}

	public String toString() {
		return Yaml.toString(this);
	}

	public CloudStorageInfo getCloudstorage() {
		return cloudstorage;
	}

	public void setCloudstorage(CloudStorageInfo cloudstorage) {
		this.cloudstorage = cloudstorage;
	}

	public GroovyDeploymentScript getDeploymentScript() {
		return deploymentScript;
	}

	public void setDeploymentScript(GroovyDeploymentScript deploymentScript) {
		this.deploymentScript = deploymentScript;
	}
	
}