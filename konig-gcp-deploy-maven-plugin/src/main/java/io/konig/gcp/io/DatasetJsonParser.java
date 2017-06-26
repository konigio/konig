package io.konig.gcp.io;

import java.io.IOException;
import java.io.Reader;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.model.Dataset;

public class DatasetJsonParser {

	
	public Dataset parse(Reader reader) throws IOException {

		JsonFactory factory = JacksonFactory.getDefaultInstance();
		JsonObjectParser parser = factory.createJsonObjectParser();
		return parser.parseAndClose(reader, Dataset.class);
	}
}
