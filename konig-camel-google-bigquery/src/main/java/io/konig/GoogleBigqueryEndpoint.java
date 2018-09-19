package io.konig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * #%L
 * Camel GoogleBigquery Component
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;


/**
 * Represents a GoogleBigquery endpoint.
 */
@UriEndpoint(scheme = "konig-google-bigquery", title = "GoogleBigquery", syntax = "konig-google-bigquery:projectId:datasetId:tableId", consumerClass = GoogleBigqueryConsumer.class, label = "GoogleBigquery")
public class GoogleBigqueryEndpoint extends DefaultEndpoint {

	@UriPath
	@Metadata(required = "true")
	private String configuration;

	private String projectId;

	private String datasetId;

	private String tableId;
	@UriParam(defaultValue = "10")
	private int option = 10;

	private BigQuery bigQuery;

	public GoogleBigqueryEndpoint() {
	}

	public GoogleBigqueryEndpoint(String uri, GoogleBigqueryComponent component) throws Exception {
		super(uri, component);
		String[] parts = uri.split(":");

		if (parts.length < 2) {
			throw new IllegalArgumentException("Google BigQuery Endpoint format \"projectId:datasetId[:tableName]\"");
		}

		int c = 1;
		projectId = parts[c++];
		datasetId = parts[c++];
		if (parts.length > 2) {
			tableId = parts[c++];
		}
		try {
			GoogleCredentials credentials = null;
			String fileName = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
			if (fileName != null) {
				FileInputStream input = new FileInputStream(fileName);
				credentials = GoogleCredentials.fromStream(input);
			}
			if (fileName == null) {
				throw new Exception("Google Credentials not found.  Please define "
						+ "'GOOGLE_APPLICATION_CREDENTIALS' environment variable");
			}
			bigQuery = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
		} catch (IOException e) {
			throw new Exception(e);
		}
	}

	public GoogleBigqueryEndpoint(String endpointUri) {
		super(endpointUri);
	}

	public Producer createProducer() throws Exception {
		return new GoogleBigqueryProducer(this, bigQuery);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		return new GoogleBigqueryConsumer(this, processor);
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Some description of this option, and what it does
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	public String getDatasetId() {
		return datasetId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getTableId() {
		return tableId;
	}

	/**
	 * Some description of this option, and what it does
	 */
	public void setOption(int option) {
		this.option = option;
	}

	public int getOption() {
		return option;
	}
}
