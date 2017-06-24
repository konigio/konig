package io.konig.deploy.gcp;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

public class GoogleCloudPlatformInfo  {
	
	@Parameter(property="konig.deploy.gcpProjectId")
	private String projectId;
	
	@Parameter(property="konig.deploy.gcpDir")
	private File gcpDir;
	
	@Parameter(property="konig.deploy.gcpDatasetsDir")
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
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (projectId != null) {
			builder.append("googleCloudPlatform/projectId: ");
			builder.append(projectId);
			builder.append('\n');
		}
		if (gcpDir != null) {
			builder.append("googleCloudPlatform/gcpDir: ");
			builder.append(gcpDir.getAbsolutePath());
			builder.append('\n');
		}
		if (datasetsDir != null) {
			builder.append("googleCloudPlatform/datsetsDir: ");
			builder.append(datasetsDir.getAbsolutePath());
			builder.append('\n');
		}
		return builder.toString();
	}
	
}
