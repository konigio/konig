package io.konig.schemagen.gcp;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.KonigException;

public class BigQueryDatasetGenerator {
	private File schemaDir;
	private File datasetDir;
	
	
	
	public BigQueryDatasetGenerator(File schemaDir, File datasetDir) {
		this.schemaDir = schemaDir;
		this.datasetDir = datasetDir;
	}


	public void run() throws KonigException, IOException {
		
		if (schemaDir.exists()) {
			ObjectMapper mapper = new ObjectMapper();
			
			File[] tableList = schemaDir.listFiles();
			for (File tableFile : tableList) {
				handleTable(mapper, tableFile);
			}
		}
	}


	private void handleTable(ObjectMapper mapper, File tableFile) throws KonigException, JsonProcessingException, IOException {
		
		JsonNode node = mapper.readTree(tableFile);
		if (node instanceof ObjectNode) {
			ObjectNode json = (ObjectNode) node;
			ObjectNode tableRef = (ObjectNode) json.get("tableReference");
			JsonNode projectId = tableRef.get("projectId");
			JsonNode datasetId = tableRef.get("datasetId");
			
			if (projectId == null) {
				throw new KonigException("tableReference.projectId property must be defined in file: " + tableFile.getAbsolutePath());
			}
			if (datasetId == null) {
				throw new KonigException("tableReference.datasetId property must be defined in file: " + tableFile.getAbsolutePath());
			}
			
			File datasetFile = new File(datasetDir, datasetId.asText() + ".json");
			if (!datasetFile.exists()) {
				datasetDir.mkdirs();
				JsonFactory factory = new JsonFactory();
				JsonGenerator generator = factory.createGenerator(datasetFile, JsonEncoding.UTF8);
				try {
					generator.useDefaultPrettyPrinter();
					
					generator.writeStartObject();
					generator.writeFieldName("datasetReference");
					generator.writeStartObject();
					generator.writeStringField("projectId", projectId.asText());
					generator.writeStringField("datasetId", datasetId.asText());
					generator.writeEndObject();
					generator.writeEndObject();
				} finally {
					generator.close();
				}
				
			}
			
			
		} else {
			throw new KonigException("File must contain a JSON Object at the top level: " + tableFile.getAbsolutePath());
		}
		
	}

}
