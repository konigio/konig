package io.konig.gcp.deployment;

/*
 * #%L
 * Konig GCP Deployment Manager
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


import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

public class DeploymentConfigWriterTest {
	
	private DeploymentConfigWriter writer = new DeploymentConfigWriter();

	@Test
	public void testBigQuery() throws Exception {
		DeploymentConfig config = new DeploymentConfig();
		
		BigqueryDatasetResource dataset = new BigqueryDatasetResource();
		BigqueryDatasetProperties datasetProperties = new BigqueryDatasetProperties();
		BigqueryDatasetReference datasetReference = new BigqueryDatasetReference();
		datasetReference.setDatasetId("example_dataset_id");
		datasetProperties.setDatasetReference(datasetReference);
		
		dataset.setName("dataset-example_dataset");
		dataset.setProperties(datasetProperties);
		
		config.addResource(dataset);
		
		BigqueryTableResource tableResource = new BigqueryTableResource();
		config.addResource(tableResource);
		tableResource.setName("person_table");
		
		BigqueryTableProperties table = new BigqueryTableProperties();
		tableResource.setProperties(table);
		table.setDatasetId("example_dataset_id");
		List<TableFieldSchema> fields = new ArrayList<>();
		TableSchema schema = new TableSchema();
		table.setSchema(schema);
		schema.setFields(fields);
		TableFieldSchema givenName = new TableFieldSchema();
		givenName.setName("givenName");
		givenName.setType("STRING");
		fields.add(givenName);
		
		GcpMetadata meta = new GcpMetadata();
		tableResource.setMetadata(meta);
		meta.addDependency("dataset-example_dataset");
		
		
		StringWriter out = new StringWriter();
		writer.write(out, config);
		
		
		String actual = out.toString().replace("\r", "");
		
		String expected = 
				"\n" +
				"resources: \n" + 
				"   - \n" + 
				"      name: dataset-example_dataset\n" + 
				"      type: gcp-types/bigquery-v2:datasets\n" + 
				"      properties: \n" + 
				"         datasetReference: \n" + 
				"            datasetId: example_dataset_id\n" +
				"   - \n" + 
				"      name: person_table\n" + 
				"      type: gcp-types/bigquery-v2:tables\n" + 
				"      metadata: \n" + 
				"         dependsOn: \n" + 
				"            - dataset-example_dataset\n" + 
				"      properties: \n" + 
				"         datasetId: example_dataset_id\n" + 
				"         schema: \n" + 
				"            fields: \n" + 
				"               - \n" + 
				"                  name: givenName\n" + 
				"                  type: STRING\n";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testStorageBucket() throws Exception {
		DeploymentConfig config = new DeploymentConfig();
		StorageBucketResource resource = new StorageBucketResource();
		resource.setName("example_bucket");
		StorageBucketProperties properties = new StorageBucketProperties();
		resource.setProperties(properties);
		properties.setName("example_bucket");
		config.addResource(resource);
		StringWriter out = new StringWriter();
		writer.write(out, config);
		
		
		String actual = out.toString().replace("\r", "");
		System.out.println(actual);
		String expected = 
				"\n" +
				"resources: \n" + 
				"   - \n" + 
				"      name: example_bucket\n" + 
				"      type: storage.v1.bucket\n" + 
				"      properties: \n" + 
				"         name: example_bucket\n" ;
		assertEquals(expected, actual);
	}			
}
