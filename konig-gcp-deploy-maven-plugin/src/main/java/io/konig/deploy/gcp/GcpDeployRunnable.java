package io.konig.deploy.gcp;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.cloud.WriteChannel;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import io.konig.gcp.common.GoogleCloudService;
import io.konig.gcp.common.GoogleCloudServiceException;
import io.konig.gcp.common.GoogleCredentialsNotFoundException;
import io.konig.gcp.common.InvalidGoogleCredentialsException;
import io.konig.maven.BigQueryInfo;
import io.konig.maven.CloudStorageInfo;
import io.konig.maven.DeployAction;
import io.konig.maven.GoogleCloudPlatformConfig;

public class GcpDeployRunnable  {
	
	private DeployAction action;
	private GoogleCloudPlatformConfig gcp;
	private GoogleCloudService gcpService;
	private String modifiedTimestamp;
	

	
	private GoogleCloudService service() throws DeploymentException {
		gcpService = new GoogleCloudService();
		try {
			gcpService.useDefaultCredentials();
		} catch (GoogleCredentialsNotFoundException | InvalidGoogleCredentialsException | IOException e) {
			throw new DeploymentException(e);
		}
		return gcpService;
	}

	public void run() throws DeploymentException {
		
		service();
		
		switch (action) {
		case CREATE:
			doCreate();
			break;
			
		case DELETE:
			doDelete();
			break;
			
		case LOAD:
			doLoad();
			break;
			
		case PREVIEW:
			doPreview();
			break;
			
		case DIFF:
		case UPDATE:
		case UPSERT:
			
		}
		
	}

	private void doPreview() throws DeploymentException {
		// TODO: Implement
		
	}

	


	private void doLoad() throws DeploymentException {
		
		loadCloudStorage(gcp.getCloudstorage());
		loadBigQuery(gcp.getBigquery());
		
	}

	private void loadBigQuery(BigQueryInfo bigQuery) throws DeploymentException {
		if (bigQuery != null) {
			File scripts = bigQuery.getScripts();
			if (scripts != null && scripts.exists() && scripts.isDirectory()) {
				File[] array = scripts.listFiles();
				for (File file : array) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith(".sql")) {
						executeBigQueryScript(file);
					}
				}
			}
		}
		
	}

	private void executeBigQueryScript(File file) throws DeploymentException {
		try {
			BigQuery bigQuery = gcpService.bigQuery();
			
			String sqlText = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
			if (modifiedTimestamp != null) {
				sqlText = sqlText.replace("{modified}", modifiedTimestamp);
			}
			
			QueryRequest request = QueryRequest.newBuilder(sqlText).setUseLegacySql(false).build();

			// TODO: capture the Job ID.
			bigQuery.query(request);
			
		} catch (Throwable e) {
			throw new DeploymentException(e);
		}
		
	}

	private void loadCloudStorage(CloudStorageInfo cloudstorage) throws DeploymentException {
		if (cloudstorage != null) {
			File data = cloudstorage.getData();
			if (data!=null && data.exists() && data.isDirectory()) {
				File[] array = data.listFiles();
				for (File file : array) {
					if (file.isDirectory()) {
						uploadToBucket(file);
					}
				}
			}
		}
		
	}

	private void uploadToBucket(File bucketDir) throws DeploymentException {
		Storage storage = gcpService.storage();
		String bucketName = bucketDir.getName();
		
		File[] array = bucketDir.listFiles();
		for (File objectFile : array) {
			String objectName = objectFile.getName();
			BlobId blobId = BlobId.of(bucketName, objectName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
			try (
				WriteChannel channel = storage.writer(blobInfo)
			) {
				try (
					FileInputStream fileInput = new FileInputStream(objectFile);
					FileChannel fileChannel = fileInput.getChannel();
				) {
					ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
					int nRead;
					while ((nRead=fileChannel.read(byteBuffer)) != -1) {
						if (nRead == 0) {
							byteBuffer.clear();
							continue;
						}
						byteBuffer.position(0);
						byteBuffer.limit( nRead );
						
						channel.write(byteBuffer);
						byteBuffer.clear();
					}
					
				}
				
			} catch (Throwable e) {
				throw new DeploymentException(e);
			}
			
		}
		
	}

	private void doDelete() throws DeploymentException {
		deleteBigQuery(gcp.getBigquery());
		deleteCloudStorage(gcp.getCloudstorage());
	}

	private void deleteCloudStorage(CloudStorageInfo info) throws DeploymentException {
		if (info != null) {
			File cloudstorage = info.getDirectory();
			if (cloudstorage != null) {
				try {
					gcpService.setGcpBucketSuffix(info.getBucketSuffix());
					gcpService.deleteAllBuckets(cloudstorage);
				} catch (IOException | GoogleCloudServiceException e) {
					throw new DeploymentException(e);
				}
			}
		}
		
	}

	private void deleteBigQuery(BigQueryInfo bigQuery) throws DeploymentException {
		if (bigQuery != null) {
			deleteSchemas(bigQuery.getSchema());
			deleteDatasets(bigQuery.getDataset());
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

		createCloudStorage(gcp.getCloudstorage());
		createBigQuery(gcp.getBigquery());
		
	}

	private void createCloudStorage(CloudStorageInfo info) throws DeploymentException {
		if (info!=null) {
			File cloudstorage = info.getDirectory();
			if (cloudstorage != null) {
				try {
					gcpService.setGcpBucketSuffix(info.getBucketSuffix());
					gcpService.createAllBuckets(cloudstorage);
				} catch (GoogleCloudServiceException | IOException e) {
					throw new DeploymentException(e);
				}
			}
		}
		
	}

	private void createBigQuery(BigQueryInfo bigQuery) throws DeploymentException {
		
		if (bigQuery != null) {
			createDatasets(bigQuery.getDataset());
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

	public GoogleCloudPlatformConfig getGoogleCloudPlatform() {
		return gcp;
	}

	public void setGoogleCloudPlatform(GoogleCloudPlatformConfig googleCloudPlatform) {
		this.gcp = googleCloudPlatform;
	}

	public String getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	public void setModifiedTimestamp(String modifiedTimestamp) {
		this.modifiedTimestamp = modifiedTimestamp;
	}

}
