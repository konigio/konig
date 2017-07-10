package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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
