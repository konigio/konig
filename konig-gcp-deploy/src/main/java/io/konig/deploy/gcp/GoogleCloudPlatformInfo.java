package io.konig.deploy.gcp;

import java.io.File;

public class GoogleCloudPlatformInfo {
	
	private String projectId;
	private File gcpDir;
	private File datasetsDir;
	
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
	public File getGcpDir() {
		return gcpDir;
	}

	/**
	 * Specify the location of the top-level directory in the local file system where
	 * Google Cloud Platform resources are stored.
	 * @param gcpDir
	 */
	public void setGcpDir(File gcpDir) {
		this.gcpDir = gcpDir;
	}

	/**
	 * Get the directory in the local file system where Google BigQuery datasets are stored.
	 */
	public File getDatasetsDir() {
		return datasetsDir;
	}

	/**
	 * Set the directory in the local file system where Google BigQuery datasets are stored.
	 */
	public void setDatasetsDir(File datasetsDir) {
		this.datasetsDir = datasetsDir;
	}
	
}
