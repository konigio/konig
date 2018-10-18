package io.konig.gcp.deployment;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

import com.google.api.services.bigquery.model.Table;

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
		
		dataset.setName("example_dataset");
		dataset.setProperties(datasetProperties);
		
		config.addResource(dataset);
		
		BigqueryTableResource tableResource = new BigqueryTableResource();
		tableResource.setName("person_table");
		
		Table table;
		
		
		StringWriter out = new StringWriter();
		writer.write(out, config);
		
		System.out.println(out.toString());
		
		String actual = out.toString().replace("\r", "");
		
		String expected = 
				"\n" +
				"resources: \n" + 
				"   - \n" + 
				"      name: example_dataset\n" + 
				"      type: bigquery.v2.dataset\n" + 
				"      properties: \n" + 
				"         datasetReference: \n" + 
				"            datasetId: example_dataset_id\n";
		
		assertEquals(expected, actual);
	}

}
