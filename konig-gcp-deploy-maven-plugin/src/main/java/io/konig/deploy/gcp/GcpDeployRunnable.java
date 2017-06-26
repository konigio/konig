package io.konig.deploy.gcp;

import java.io.File;
import java.io.IOException;

import com.google.cloud.bigquery.BigQuery;

import io.konig.deploy.DeployAction;
import io.konig.gcp.common.GoogleCloudService;
import io.konig.gcp.common.GoogleCredentialsNotFoundException;
import io.konig.gcp.common.InvalidGoogleCredentialsException;

public class GcpDeployRunnable  {
	
	private DeployAction action;
	private GoogleCloudPlatformInfo gcp;
	private GoogleCloudService gcpService;
	
	public void run() throws DeploymentException {
		
		gcpService = new GoogleCloudService();
		try {
			gcpService.useDefaultCredentials();
		} catch (GoogleCredentialsNotFoundException | InvalidGoogleCredentialsException | IOException e) {
			throw new DeploymentException(e);
		}
		
		switch (action) {
		case CREATE:
			doCreate();
			break;
			
		case DELETE:
			doDelete();
			break;
			
		case DIFF:
		case UPDATE:
		case UPSERT:
			
		}
		
		
		
	}

	private void doDelete() throws DeploymentException {
		deleteBigQuery(gcp.getBigQuery());
	}

	private void deleteBigQuery(BigQueryInfo bigQuery) throws DeploymentException {
		if (bigQuery != null) {
			deleteSchemas(bigQuery.getSchema());
			deleteDatasets(bigQuery.getDatasets());
		}
		
	}

	private void deleteSchemas(File schema) throws DeploymentException {
		
		BigQuery bigQuery = gcpService.bigQuery();
		try {
			gcpService.deleteAllTables(bigQuery, schema);
		} catch (Throwable e) {
			throw new DeploymentException(e);
		}
		
	}

	private void deleteDatasets(File datasetsDir) throws DeploymentException {

		BigQuery bigQuery = gcpService.bigQuery();
		try {
			gcpService.deleteAllDatasets(bigQuery, datasetsDir);
		} catch (IOException e) {
			throw new DeploymentException(e);
		}
		
	}

	private void doCreate() throws DeploymentException {
	
		createBigQuery(gcp.getBigQuery());
		
	}

	private void createBigQuery(BigQueryInfo bigQuery) throws DeploymentException {
		
		if (bigQuery != null) {
			createDatasets(bigQuery.getDatasets());
			createTables(bigQuery.getSchema());
		}
		
	}

	private void createTables(File tables) throws DeploymentException {
		if (tables != null) {

			BigQuery bigQuery = gcpService.bigQuery();
			try {
				gcpService.createAllTables(bigQuery, tables);
			} catch (IOException e) {
				throw new DeploymentException(e);
			}
		}
		
	}

	private void createDatasets(File datasetsDir) throws DeploymentException {

		if (datasetsDir != null) {

			BigQuery bigQuery = gcpService.bigQuery();
			try {
				gcpService.createAllDatasets(bigQuery, datasetsDir);
			} catch (IOException e) {
				throw new DeploymentException(e);
			}
		}
		
	}

	public DeployAction getAction() {
		return action;
	}

	public void setAction(DeployAction action) {
		this.action = action;
	}

	public GoogleCloudPlatformInfo getGoogleCloudPlatform() {
		return gcp;
	}

	public void setGoogleCloudPlatform(GoogleCloudPlatformInfo googleCloudPlatform) {
		this.gcp = googleCloudPlatform;
	}

}
