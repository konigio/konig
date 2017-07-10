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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableInfo;

public class GoogleCloudServiceIT {
	
	@Test
	public void testTableData() throws Exception {
		
		GoogleCloudService service = new GoogleCloudService();
		service.useDefaultCredentials();
		
		System.out.println(service.getProjectId());
		
		DatasetInfo datasetInfo = service.readDatasetInfo(new File("src/test/resources/GoogleCloudServiceIT/person-extent/gcp/bigquery/dataset/schema.json"));
		TableInfo tableInfo = service.readTableInfo(new File("src/test/resources/GoogleCloudServiceIT/person-extent/gcp/bigquery/schema/schema.Person.json"));
		
		
		BigQuery bigquery = service.bigQuery();
		
		Dataset dataset = bigquery.getDataset(datasetInfo.getDatasetId());
		if (dataset != null) {
			service.forceDelete(dataset);
		}
		
		dataset = bigquery.create(datasetInfo);
		
		Table table = bigquery.create(tableInfo);
		
		service.insertJson(table, new File("src/test/resources/GoogleCloudServiceIT/person-extent/gcp/bigquery/data/schema.Person.jsonl"));
		
		
		QueryRequest request = QueryRequest.newBuilder("SELECT * FROM schema.Person ORDER BY givenName").setDefaultDataset(dataset.getDatasetId()).build();
		BigQueryOptions options = BigQueryOptions.newBuilder().setProjectId(service.getProjectId()).build();
		QueryResponse response = bigquery.query(request);
		
		while (!response.jobCompleted()) {
			Thread.sleep(1000);
			response = bigquery.getQueryResults(response.getJobId());
		}
		
		assertTrue(!response.hasErrors());
		
		QueryResult result = response.getResult();
		Iterator<List<FieldValue>> sequence = result.iterateAll().iterator();
		assertTrue(sequence.hasNext());
		List<FieldValue> alice = sequence.next();
		assertEquals(2, alice.size());
		FieldValue givenName = alice.get(0);
		assertEquals("Alice", givenName.getStringValue());
		
		FieldValue familyName = alice.get(1);
		assertEquals("Smith", familyName.getStringValue());
		
		assertTrue(sequence.hasNext());
		List<FieldValue> bob = sequence.next();
		assertEquals(2, bob.size());
		givenName = bob.get(0);
		assertEquals("Bob", givenName.getStringValue());
		
		familyName = bob.get(1);
		assertEquals("Jones", familyName.getStringValue());
	}
	
	@Ignore
	public void testTable() throws Exception {
		
		GoogleCloudService service = new GoogleCloudService();
		service.useDefaultCredentials();
		
		DatasetInfo datasetInfo = service.readDatasetInfo(new File("src/test/resources/GoogleCloudServiceIT/person-extent/gcp/bigquery/dataset/schema.json"));
		TableInfo tableInfo = service.readTableInfo(new File("src/test/resources/GoogleCloudServiceIT/person-extent/gcp/bigquery/schema/schema.Person.json"));
		
		
		BigQuery bigquery = service.bigQuery();
		Dataset dataset = bigquery.getDataset(datasetInfo.getDatasetId());
		if (dataset != null) {
			service.forceDelete(dataset);
		}
		
		dataset = bigquery.create(datasetInfo);
		
		bigquery.create(tableInfo);
		
		Table loaded = bigquery.getTable(tableInfo.getTableId());
		Schema schema = loaded.getDefinition().getSchema();
		List<Field> fieldList = schema.getFields();
		assertEquals(2, fieldList.size());
		
		Field givenName = fieldList.get(0);
		assertEquals("givenName", givenName.getName());
		assertEquals("STRING", givenName.getType().getValue().name());
		
		Field familyName = fieldList.get(1);
		assertEquals("familyName", familyName.getName());
		assertEquals("STRING", familyName.getType().getValue().name());
		
		
	}

	@Ignore
	public void testDataset() throws Exception {
		
		try (
			FileReader reader = new FileReader("src/test/resources/GoogleCloudServiceIT/person-extent/gcp/bigquery/dataset/schema.json");
		) {
			GoogleCloudService service = new GoogleCloudService();
			service.useDefaultCredentials();
		
			
			BigQuery bigquery = service.bigQuery();
			
			DatasetInfo dataset = service.readDatasetInfo(reader);
			bigquery.create(dataset);
			
			DatasetInfo loaded = bigquery.getDataset(dataset.getDatasetId());
			assertEquals(loaded.getDatasetId().getDataset(), dataset.getDatasetId().getDataset());
			assertEquals(loaded.getDatasetId().getProject(), dataset.getDatasetId().getProject());
			
			bigquery.delete(dataset.getDatasetId());
			
		}
		
	}
	
	

}
