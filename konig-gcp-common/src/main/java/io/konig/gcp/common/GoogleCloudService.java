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
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.KonigBigQueryUtil;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableInfo;

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
public class GoogleCloudService {

	private String projectToken = "{gcpProjectId}";
	private GoogleCredentials credentials;
	private String projectId;

	
	public GoogleCloudService() {
	}
	
	public List<DatasetInfo> createAllDatasets(BigQuery bigQuery, File datasetDir) throws IOException {
		List<DatasetInfo> list = new ArrayList<>();
		if (bigQuery == null) {
			bigQuery = bigQuery();
		}
		File[] array = datasetDir.listFiles();
		for (File file : array) {
			try (FileReader reader = new FileReader(file)) {
				DatasetInfo info = readDatasetInfo(reader);
				list.add(info);
				bigQuery.create(info);
			}
		}
		
		return list;
	}
	
	public void deleteAllDatasets(BigQuery bigQuery, File datasetDir) throws IOException {
		if (bigQuery == null) {
			bigQuery = bigQuery();
		}
		File[] array = datasetDir.listFiles();
		for (File file : array) {
			deleteDataset(bigQuery, file);
		}
	}
	
	public void deleteDataset(BigQuery bigQuery, File datasetInfo) throws IOException {
		try (FileReader reader = new FileReader(datasetInfo)) {
			DatasetInfo info = readDatasetInfo(reader);
			if (bigQuery == null) {
				bigQuery = bigQuery();
			}
			bigQuery.delete(info.getDatasetId());
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
		projectId = readProjectId(jsonKey);
		try (
			FileInputStream input = new FileInputStream(jsonKey);
		) {
			credentials = GoogleCredentials.fromStream(input);
		}
	}

	private String readProjectId(File jsonKey) throws IOException, InvalidGoogleCredentialsException {
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
		List<Table> list = toList(dataset.list().iterateAll());
		for (Table t : list) {
			t.delete();
		}
		dataset.delete();
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
	
	public TableInfo readTableInfo(Reader reader) throws IOException {
		JsonFactory factory = JacksonFactory.getDefaultInstance();
		JsonObjectParser parser = factory.createJsonObjectParser();
		ReplaceStringReader input = new ReplaceStringReader(reader, projectToken, projectId);

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
	
	
	public BigQuery bigQuery() {
		return BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
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
