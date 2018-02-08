package io.konig.gcp.io;

/*
 * #%L
 * Konig Google Cloud Platform Model
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


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.konig.core.HasURI;
import io.konig.gcp.datasource.GoogleCloudSqlBackendType;
import io.konig.gcp.datasource.GoogleCloudSqlDatabase;
import io.konig.gcp.datasource.GoogleCloudSqlInstance;
import io.konig.gcp.datasource.GoogleCloudSqlInstanceType;
import io.konig.gcp.datasource.GoogleCloudSqlRegion;
import io.konig.gcp.datasource.GoogleCloudSqlTableInfo;
import io.konig.gcp.datasource.GoogleCloudSqlVersion;

public class GoogleCloudSqlJsonUtil {

	public static GoogleCloudSqlInstance readJson(Reader reader) throws JsonParseException, IOException {
		GoogleCloudSqlInstance instance = new GoogleCloudSqlInstance();
		
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(reader);
		
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			JsonToken token = parser.getCurrentToken();
			if (token == JsonToken.FIELD_NAME) {
				String fieldName = parser.getCurrentName();
				parser.nextToken();
				String value = parser.getText();
				
				switch (fieldName) {
				case "selfLink" :
					instance.setId(new URIImpl(value));
					break;
					
				case "backendType" :
					instance.setBackendType(GoogleCloudSqlBackendType.fromLocalName(value));
					break;
					
				case "databaseVersion" :
					instance.setDatabaseVersion(GoogleCloudSqlVersion.fromLocalName(value));
					break;
					
				case "instanceType" :
					instance.setInstanceType(GoogleCloudSqlInstanceType.fromLocalName(value));
					break;
					
				case "name" :
					instance.setName(value);
					break;
					
				case "region" :
					instance.setRegion(GoogleCloudSqlRegion.fromLocalName(value));
				}
			}
		}
		
		return instance;
	}
	
	public static void writeJson(GoogleCloudSqlTableInfo table, Writer writer) throws IOException {
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(writer);
		json.useDefaultPrettyPrinter();
		
		json.writeStartObject();
		if (table.getId()!= null) {
			json.writeStringField("selfLink", table.getId().stringValue());
		}
		writeString(json, "instance", table.getInstance());
		writeString(json, "database", table.getDatabase());
		writeString(json, "name", table.getTableName());
		if (table.getDdlFile()!=null) {
			writeString(json, "ddlFile", table.getDdlFile().getName());
		}
		writeString(json,"instanceFile", table.getInstanceFile().getName());
		json.writeEndObject();
		json.flush();
	}

	public static void writeJson(GoogleCloudSqlInstance instance, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(writer);
		json.useDefaultPrettyPrinter();
		
		json.writeStartObject();
		if (instance.getId()!= null) {
			json.writeStringField("selfLink", instance.getId().stringValue());
		}
		writeString(json, "name", instance.getName());
		writeLocalName(json, "backendType", instance.getBackendType());
		writeLocalName(json, "databaseVersion", instance.getDatabaseVersion());
		writeLocalName(json, "instanceType", instance.getInstanceType());
		writeLocalName(json, "region", instance.getRegion());
		json.writeFieldName("settings");
		json.writeStartObject();
		writeLocalName(json,"tier",instance.getSettings().getTier());
		json.writeEndObject();
		json.writeEndObject();
		
		json.flush();
		
		
	}

	public static void writeJson(GoogleCloudSqlDatabase db, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(writer);
		json.useDefaultPrettyPrinter();
		
		json.writeStartObject();
		if (db.getId()!= null) {
			json.writeStringField("selfLink", db.getId().stringValue());
		}
		writeString(json, "name", db.getName());
		writeString(json, "instance", db.getInstance());
		
		json.writeEndObject();
		
		json.flush();
		
		
	}

	private static void writeString(JsonGenerator json, String fieldName, String value) throws IOException {
		
		if (value != null) {
			json.writeStringField(fieldName, value);
		}
	}

	private static void writeLocalName(JsonGenerator json, String fieldName, HasURI object) throws IOException {
		
		if (object != null) {
			URI uri = object.getURI();
			if (uri != null) {
				json.writeStringField(fieldName, uri.getLocalName());
			}
		}
		
	}

}
