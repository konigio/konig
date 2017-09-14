package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Common
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.KonigBigQueryUtil;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.CloudStorageUtil;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 * A utility class for manipulating Google Cloud Platform resources.
 * <p>
 * Typical usage:
 * <pre>
 *  GoogleCloudService gcs = new GoogleCloudService();
 *  gcs.useDefaultCredentials();
 *  
 *  BigQuery bigQuery = gcs.bigQuery();
 *  DatasetInfo datasetInfo = gcs.readDatasetInfo(someFile);
 *  bigQuery.create(datasetInfo);
 * </pre>
 * </p>
 * <p>
 * The {@link #useDefaultCredentials()} method loads a file containing a service account
 * key in JSON format from a location specified by the <code>google.credentials</code>
 * System property.  If that property is not defined, it will use the <code>GOOGLE_APPLICATION_CREDENTIALS</code>
 * environment variable.
 * </p>
 * Alternatively, you can use the {@link #openCredentials(File)} method to specify the location of the 
 * service account key.
 * 
 * @author Greg McFall
 *
 */
public class GoogleCloudService implements CredentialsProvider {

	private File credentialsFile;
	private String gcpBucketSuffix;
	private String gcpBucketSuffixToken = "{gcpBucketSuffix}";
	private String projectToken = "{gcpProjectId}";
	private GoogleCredentials credentials;
	private String projectId;
	private BigQuery bigQuery;
	private Storage storage;
	private TopicAdminClient topicAdmin;
	

	
	public GoogleCloudService() {
	}
	
	
	public String getGcpBucketSuffix() {
		return gcpBucketSuffix;
	}


	public void setGcpBucketSuffix(String gcpBucketSuffix) {
		this.gcpBucketSuffix = gcpBucketSuffix;
	}
	
	public List<BucketInfo> createAllBuckets(File storageDir) throws IOException, GoogleCloudServiceException {
		List<BucketInfo> list = new ArrayList<>();
	
		File[] array = storageDir.listFiles();
		if (array != null) {
			Storage storage = storage();
			for (File file : array) {
				BucketInfo info = readBucketInfo(file);
				list.add(info);
				storage.create(info);
			}
		}
		return list;
	}
	


	public List<TableInfo> createAllTables(BigQuery bigQuery, File schemaDir) throws IOException {
		List<TableInfo> list = new ArrayList<>();
		File[] array = schemaDir.listFiles();
		for (File file : array) {
			list.add(createBigQueryTable(file));
		}
		return list;
	}
	
	public TableInfo createBigQueryTable(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			TableInfo info = readTableInfo(reader);
			bigQuery.create(info);
			return info;
		}
	}
	
	public Storage storage() {
		if (storage == null) {
			storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
		}
		return storage;
	}
	
	public List<DatasetInfo> createAllDatasets(BigQuery bigQuery, File datasetDir) throws IOException {
		List<DatasetInfo> list = new ArrayList<>();
		if (bigQuery == null) {
			bigQuery = bigQuery();
		}
		File[] array = datasetDir.listFiles();
		if (array != null) {
			for (File file : array) {
				list.add(createDataset(file));
			}
		}
		
		return list;
	}
	
	

	public DatasetInfo createDataset(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			DatasetInfo info = readDatasetInfo(reader);
			bigQuery.create(info);
			return info;
		}
		
	}


	public void deleteAllDatasets(BigQuery bigQuery, File datasetDir) throws IOException {
		if (bigQuery == null) {
			bigQuery = bigQuery();
		}
		if (datasetDir != null) {

			File[] array = datasetDir.listFiles();
			if (array != null) {
				for (File file : array) {
					deleteDataset(bigQuery, file);
				}
			}
		}
	}

	public void deleteAllBuckets(File storageDir) throws IOException, GoogleCloudServiceException {
		
		File[] array = storageDir.listFiles();
		if (array != null) {
			Storage storage = storage();
			for (File file : array) {
				BucketInfo info = readBucketInfo(file);
				Bucket bucket = storage.get(info.getName());
				deleteBucket(bucket);
			}
		}
	}
	
	private void deleteBucket(Bucket bucket) {
		if (bucket != null) {
		
			Page<Blob> page = bucket.list();
			
			for (Blob blob : page.iterateAll()) {
				blob.delete();
			}

			bucket.delete();
		}
		
	}


	public void deleteAllTables(BigQuery bigQuery, File schemaDir) throws IOException {
		if (bigQuery == null) {
			bigQuery = bigQuery();
		}
		if (schemaDir != null) {

			File[] array = schemaDir.listFiles();
			if (array!=null) {
				for (File file : array) {
					deleteTable(bigQuery, file);
				}
			}
		}
	}
	
	public void deleteTable(BigQuery bigQuery, File tableSchemaFile) throws FileNotFoundException, IOException {
		try (FileReader reader = new FileReader(tableSchemaFile)) {
			TableInfo info = readTableInfo(reader);
			if (bigQuery == null) {
				bigQuery = bigQuery();
			}
			
			bigQuery.delete(info.getTableId());
		}
		
	}


	public void deleteDataset(BigQuery bigQuery, File datasetInfo) throws IOException {
		try (FileReader reader = new FileReader(datasetInfo)) {
			DatasetInfo info = readDatasetInfo(reader);
			if (bigQuery == null) {
				bigQuery = bigQuery();
			}
			Dataset dataset = bigQuery.getDataset(info.getDatasetId());
			forceDelete(dataset);
		}
	}
	
	public void useDefaultCredentials() throws GoogleCredentialsNotFoundException, InvalidGoogleCredentialsException, IOException {
		String fileName = System.getProperty("google.credentials");
		if (fileName == null) {
			fileName = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
		}
		if (fileName == null) {
			throw new GoogleCredentialsNotFoundException();
		}
		File jsonKey = new File(fileName);
		openCredentials(jsonKey);
	}
	
	public void openCredentials(File jsonKey) throws InvalidGoogleCredentialsException, IOException {
		credentialsFile = jsonKey;
		projectId = readProjectId(jsonKey);
		try (
			FileInputStream input = new FileInputStream(jsonKey);
		) {
			credentials = GoogleCredentials.fromStream(input);
		}
	}
	
	

	public File getCredentialsFile() {
		return credentialsFile;
	}


	public String readProjectId(File jsonKey) throws IOException, InvalidGoogleCredentialsException {
		try (
			FileInputStream input = new FileInputStream(jsonKey)
		) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.reader().readTree(input);
			if (node instanceof ObjectNode) {
				ObjectNode obj = (ObjectNode) node;
				node = obj.get("project_id");
				if (node != null) {
					return node.asText();
				}
			}
		}
		
		throw new InvalidGoogleCredentialsException(jsonKey.getAbsolutePath());
	}
	
	/**
	 * Delete all the tables in the dataset and then delete the dataset
	 */
	public void forceDelete(Dataset dataset) {
		if (dataset != null) {

			List<Table> list = toList(dataset.list().iterateAll());
			for (Table t : list) {
				t.delete();
			}
			dataset.delete();
		}
	}
	
	
	private <T> List<T> toList(Iterable<T> sequence) {
		List<T> list = new ArrayList<>();
		for (T t : sequence) {
			list.add(t);
		}
		return list;
	}

	public TableInfo readTableInfo(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			return readTableInfo(reader);
		}
	}

	public BucketInfo readBucketInfo(File file) throws IOException, GoogleCloudServiceException {
		try (
			FileReader reader = new FileReader(file);
		) {
			return readBucketInfo(reader);
		}
	}
	
	public BucketInfo readBucketInfo(Reader reader) throws GoogleCloudServiceException, IOException {
		if (gcpBucketSuffix == null) {
			throw new GoogleCloudServiceException("gcpBucketSuffix must be defined");
		}
		JsonFactory factory = JacksonFactory.getDefaultInstance();
		JsonObjectParser parser = factory.createJsonObjectParser();
		ReplaceStringReader input = new ReplaceStringReader(reader, gcpBucketSuffixToken, gcpBucketSuffix);
		com.google.api.services.storage.model.Bucket bucket = 
			parser.parseAndClose(input, com.google.api.services.storage.model.Bucket.class); 
		
		return CloudStorageUtil.createBucketInfo(bucket);
	}
	
	public TableInfo readTableInfo(Reader reader) throws IOException {
		JsonFactory factory = JacksonFactory.getDefaultInstance();
		JsonObjectParser parser = factory.createJsonObjectParser();
		ReplaceStringsReader input = new ReplaceStringsReader(
			reader, projectToken, projectId, gcpBucketSuffixToken, gcpBucketSuffix);

		com.google.api.services.bigquery.model.Table model = 
			parser.parseAndClose(input, com.google.api.services.bigquery.model.Table.class);
		
		return KonigBigQueryUtil.createTableInfo(model);
	}
	
	public DatasetInfo readDatasetInfo(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			return readDatasetInfo(reader);
		}
	}
	
	public InsertAllResponse insertJson(Table table, Reader jsonData) throws IOException {
		BigQueryDataFile dataFile = BigQueryDataFile.jsonFile(jsonData);
		return table.insert(dataFile.iterable());
	}
	
	public InsertAllResponse insertJson(Table table, File jsonData) throws IOException {
		try (FileReader reader = new FileReader(jsonData)) {
			return insertJson(table, reader);
		}
	}
	
	
	public DatasetInfo readDatasetInfo(Reader reader) throws IOException {
		
		JsonFactory factory = JacksonFactory.getDefaultInstance();
		JsonObjectParser parser = factory.createJsonObjectParser();
		ReplaceStringReader input = new ReplaceStringReader(reader, projectToken, projectId);

		com.google.api.services.bigquery.model.Dataset model = 
			parser.parseAndClose(input, com.google.api.services.bigquery.model.Dataset.class);
		
		return KonigBigQueryUtil.createDatasetInfo(model);
	}
	
	public TopicAdminClient topicAdmin() {
		if (topicAdmin == null) {
			try {
				TopicAdminSettings settings = TopicAdminSettings.defaultBuilder()
						.setCredentialsProvider(this).build();
				
				topicAdmin = TopicAdminClient.create(settings);
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
		return topicAdmin;
	}
	
	public BigQuery bigQuery() {
		if (bigQuery == null) {
			bigQuery = BigQueryOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
		}
		return bigQuery;
	}

	public String getProjectToken() {
		return projectToken;
	}

	public GoogleCredentials getCredentials() {
		return credentials;
	}

	public String getProjectId() {
		return projectId;
	}
	

}
