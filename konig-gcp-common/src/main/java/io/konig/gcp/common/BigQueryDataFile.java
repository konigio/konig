package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Common
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;

public class BigQueryDataFile implements AutoCloseable {

	private BufferedReader reader;
	private Iterable<InsertAllRequest.RowToInsert> iterable;
	
	private BigQueryDataFile(BufferedReader reader) {
		this.reader = reader;
	}
	
	public static BigQueryDataFile jsonFile(File file) throws IOException {
		FileReader reader = new FileReader(file);
		BigQueryDataFile result = new BigQueryDataFile(new BufferedReader(reader));
		result.openJson();
		return result;
	}
	
	public static BigQueryDataFile jsonFile(Reader reader) throws IOException {
		BufferedReader buffer = (reader instanceof BufferedReader) ? (BufferedReader)reader : new BufferedReader(reader);
		BigQueryDataFile result = new BigQueryDataFile(buffer);
		result.openJson();
		return result;
	}
	
	private void openJson() throws IOException {
		iterable = new JsonIterable(reader);
	}

	public Iterable<InsertAllRequest.RowToInsert> iterable() {
		return iterable;
	}

	@Override
	public void close() throws Exception {
		reader.close();
		reader = null;
	}
	
	static class JsonIterable implements Iterable<InsertAllRequest.RowToInsert> {
		
		private BufferedReader reader;
		public JsonIterable(BufferedReader reader) {
			this.reader = reader;
		}

		@Override
		public Iterator<RowToInsert> iterator() {
			return new JsonIterator(reader);
		}
		
	}
	
	static class JsonIterator implements Iterator<RowToInsert> {
		private BufferedReader reader;
		private String line;
		
		JsonIterator(BufferedReader reader) {
			this.reader = reader;
			line = nextLine();
		}

		private String nextLine() {
			line = null;
			if (reader != null) {
				try {
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (line.length()!=0) {
							break;
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			return line;
		}

		

		@Override
		public boolean hasNext() {
			return line != null;
		}

		@Override
		public RowToInsert next() {
			
			Map<String,?> map = parseLine();
			line = nextLine();
			
			return RowToInsert.of(map);
		}

		private Map<String, ?> parseLine() {
			
			JsonFactory factory = new JsonFactory();
			try (
				JsonParser json = factory.createParser(line.getBytes())
			) {
				if (json.nextToken() != JsonToken.START_OBJECT) {
					throw new RuntimeException("Expected '{'");
				}
				return parseObject(json);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		private Map<String, Object> parseObject(JsonParser json) throws JsonParseException, IOException {
			Map<String,Object> map = new HashMap<>();
			
			while (json.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = json.getCurrentName();
				json.nextToken();
				Object fieldValue = value(json);
				map.put(fieldName, fieldValue);
			}
				
			
			return map;
		}

		private Object value(JsonParser json) throws IOException {
			JsonToken token = json.getCurrentToken();
			Object fieldValue = null;
			switch (token) {
			case VALUE_NUMBER_INT :
				fieldValue = new Long(json.getLongValue());
				break;
				
			case VALUE_FALSE :
				fieldValue = Boolean.FALSE;
				break;
				
			case VALUE_TRUE :
				fieldValue = Boolean.TRUE;
				break;
				
			case VALUE_NUMBER_FLOAT :
				fieldValue = new Double(json.getDoubleValue());
				break;
				
			case VALUE_STRING :
				fieldValue = json.getText();
				break;
				
			case START_OBJECT :
				fieldValue = parseObject(json);
				break;
				
			case START_ARRAY :
				fieldValue = parseArray(json);
				break;
				
			default :
				throw new RuntimeException("Invalid JSON Object");
			}
			
			return fieldValue;
		}

		private Object parseArray(JsonParser json) throws JsonParseException, IOException {
			List<Object> list = new ArrayList<>();
			while (json.nextToken() != JsonToken.END_ARRAY) {
				Object value = value(json);
				list.add(value);
			}
			return list;
		}

		@Override
		public void remove() {
			throw new RuntimeException("Remove method not supported");
			
		}
		
	}

}
