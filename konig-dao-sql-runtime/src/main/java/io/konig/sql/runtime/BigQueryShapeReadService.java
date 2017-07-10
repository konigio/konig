package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;

public class BigQueryShapeReadService extends SqlShapeReadService {

	private BigQuery bigQuery;
	
	public BigQueryShapeReadService(EntityStructureService structureService, BigQuery bigQuery) {
		super(structureService);
		this.bigQuery = bigQuery;
	}

	@Override
	protected void executeSql(EntityStructure struct, String sql, Writer output, Format format) throws DaoException {
		BigQuery bigQuery = getBigQuery();
		QueryRequest request = QueryRequest.newBuilder(sql).build();
		QueryResponse response = bigQuery.query(request);
		while (!response.jobCompleted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			response = bigQuery.getQueryResults(response.getJobId());
		}
		// TODO: look for execution errors
		QueryResult result = response.getResult();
		try {
			switch(format) {
			case JSONLD:
				writeJsonld(struct, result, output);
				break;
			}
		} catch (IOException e) {
			throw new DaoException(e);
		}
		
	}
	
	private void writeJsonld(EntityStructure struct, QueryResult result, Writer output) throws IOException, DaoException {
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(output);
		json.useDefaultPrettyPrinter();
		
		
		// TODO: Use Linked Data Platform conventions, not a top-level array.
		json.writeStartArray();
		
		Iterator<List<FieldValue>> sequence = result.iterateAll().iterator();
		
		while (sequence.hasNext()) {
			List<FieldValue> row = sequence.next();
			writeRow(struct, row, json);
		}
		
		json.writeEndArray();
		json.flush();
		
	}

	private void writeRow(EntityStructure struct, List<FieldValue> row, JsonGenerator json) throws IOException, DaoException {
		json.writeStartObject();
		Iterator<FieldInfo> fieldSequence = struct.getFields().iterator();
		for (FieldValue field : row) {
			FieldInfo fieldInfo = fieldSequence.next();
			writeField(json, fieldInfo, field);
		}
		json.writeEndObject();
		
	}

	private void writeField(JsonGenerator json, FieldInfo fieldInfo, FieldValue field) throws IOException, DaoException {
		
		String fieldName = fieldInfo.getName();
		Object value = field.getValue();
		
		if (value instanceof String) {
			json.writeStringField(fieldName, value.toString());
		} else {
			throw new DaoException("Field type not supported: " + value.getClass().getName());
		}
		
	}

	protected BigQuery getBigQuery() {
		return bigQuery;
	}

}
