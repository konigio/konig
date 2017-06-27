package io.konig.deploy.gcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.google.cloud.WriteChannel;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import io.konig.deploy.DeployAction;
import io.konig.gcp.common.GoogleCloudService;
import io.konig.gcp.common.GoogleCloudServiceException;
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
			
		case LOAD:
			doLoad();
			break;
			
		case DIFF:
		case UPDATE:
		case UPSERT:
			
		}
		
		
		
	}

	private void doLoad() throws DeploymentException {
		
		loadCloudStorage(gcp.getCloudstorage());
		
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
		deleteBigQuery(gcp.getBigQuery());
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
		createBigQuery(gcp.getBigQuery());
		
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

	public GoogleCloudPlatformInfo getGoogleCloudPlatform() {
		return gcp;
	}

	public void setGoogleCloudPlatform(GoogleCloudPlatformInfo googleCloudPlatform) {
		this.gcp = googleCloudPlatform;
	}

}
