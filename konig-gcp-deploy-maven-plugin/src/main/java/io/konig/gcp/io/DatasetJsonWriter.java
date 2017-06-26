package io.konig.gcp.io;

import java.io.IOException;
import java.io.Writer;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.model.Dataset;

public class DatasetJsonWriter {
	
	public void writeJson(Dataset dataset, Writer out) throws IOException {
		JacksonFactory factory = new JacksonFactory();
		JsonGenerator json = factory.createJsonGenerator(out);
		json.serialize(dataset);
		json.flush();
	}

}
