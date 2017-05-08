package io.konig.gcp.io;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.google.api.services.bigquery.model.Dataset;

public class DatasetJsonIoTest {

	@Test
	public void test() throws Exception {
		Dataset dataset = new Dataset();
		dataset.setId("someDataset");
		dataset.setFriendlyName("My Test Dataset");
		
		DatasetJsonWriter writer = new DatasetJsonWriter();
		StringWriter out = new StringWriter();
		
		writer.writeJson(dataset, out);
		String jsonValue = out.toString();
		
		StringReader reader = new StringReader(jsonValue);
		DatasetJsonParser parser = new DatasetJsonParser();
		Dataset parsedDataset = parser.parse(reader);
		
		assertEquals("someDataset", parsedDataset.getId());
		assertEquals("My Test Dataset", parsedDataset.getFriendlyName());
	}

}
