package io.konig.gcp.io;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetReference;

public class DatasetJsonIoTest {

	@Test
	public void test() throws Exception {
		
		// Create a dataset POJO
		Dataset dataset = new Dataset();
		DatasetReference ref = new DatasetReference();
		ref.setDatasetId("someDataset");
		ref.setProjectId("someProject");
		dataset.setDatasetReference(ref);
		dataset.setFriendlyName("My Test Dataset");
	
		
		// Write the dataset instance to a JSON string
		
		DatasetJsonWriter writer = new DatasetJsonWriter();
		StringWriter out = new StringWriter();
		
		writer.writeJson(dataset, out);
		String jsonValue = out.toString();
		
		// Parse the JSON string back into a Dataset POJO
		
		StringReader reader = new StringReader(jsonValue);
		DatasetJsonParser parser = new DatasetJsonParser();
		Dataset parsedDataset = parser.parse(reader);
		
		// Verified that the parsed Dataset has the correct properties.
		
		assertEquals("someProject", parsedDataset.getDatasetReference().getProjectId());
		assertEquals("someDataset", parsedDataset.getDatasetReference().getDatasetId());
		assertEquals("My Test Dataset", parsedDataset.getFriendlyName());
	}

}
