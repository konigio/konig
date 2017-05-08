package io.konig.gcp.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.Dataset;

public class DatasetJsonParser {

	public Dataset parse(InputStream input) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(input, Dataset.class);
	}
	
	public Dataset parse(Reader reader) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(reader, Dataset.class);
	}
}
