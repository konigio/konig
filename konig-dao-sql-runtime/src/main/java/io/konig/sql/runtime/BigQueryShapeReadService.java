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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.konig.dao.core.ShapeQuery;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import io.konig.dao.core.ChartSeriesFactory;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;

public class BigQueryShapeReadService extends SqlShapeReadService {
	private BigQuery bigQuery;
	
	public BigQueryShapeReadService(EntityStructureService structureService, BigQuery bigQuery) {
		super(structureService);
		this.bigQuery = bigQuery;		
	}

	@Override
	protected void executeSql(EntityStructure struct, ShapeQuery query, Writer output, Format format) throws DaoException {
		BigQuery bigQuery = getBigQuery();

		String sql = toSql(struct, query);

		QueryRequest.Builder querybuilder  = QueryRequest.newBuilder(sql).setUseLegacySql(false);

		if(query.getCursor() != null) {
			long pageSize = (query.getLimit()!= null) ? query.getLimit() : 1000L ;
			querybuilder.setPageSize(pageSize);
		}

		QueryRequest request = querybuilder.build();

		QueryResponse response = bigQuery.query(request);
		while (!response.jobCompleted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			response = bigQuery.getQueryResults(response.getJobId());
		}

		if(response.jobCompleted() && query.getCursor() != null && !query.getCursor().equals("")) {
			response = bigQuery.getQueryResults(response.getJobId(), BigQuery.QueryResultsOption.pageToken(query.getCursor()));
		}

		if (response.hasErrors() && response.getExecutionErrors().size() != 0) {
			throw new DaoException(response.getExecutionErrors().get(0).getMessage());
		}

		QueryResult result = response.getResult();
		handleResult(struct, result, output, format);
		
	}

	private void handleResult(EntityStructure struct, QueryResult result, Writer output, Format format) throws DaoException {
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

		json.writeStartObject();

		json.writeStringField("type", "BasicContainer");

		if(result.hasNextPage()) {
			String nextPageToken = result.getNextPageToken();
			json.writeStringField("nextPageToken", nextPageToken);
		}

		json.writeFieldName("contains");
		json.writeStartArray();

		for (List<FieldValue> row : result.iterateAll()) {
			writeRow(struct, row, json);
		}
		
		json.writeEndArray();
		json.writeEndObject();
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
		String fieldType = fieldInfo.getFieldType() == null ? "" : fieldInfo.getFieldType().toString();
		Object value = BigQueryUtil.dataValue(field, fieldType);	

		if (value instanceof String || value instanceof DateTime || value instanceof Date) {
			json.writeStringField(fieldName, value.toString());

		} else if (value instanceof Integer) {
			json.writeNumberField(fieldName, (Integer) value);

		} else if (value instanceof Long) {
			json.writeNumberField(fieldName, (Long) value);

		} else if (value instanceof Double) {
			json.writeNumberField(fieldName, (Double) value);

		} else if (value instanceof Float) {
			json.writeNumberField(fieldName, (Float) value);

		} else if (value instanceof Boolean) {
			json.writeBooleanField(fieldName, (Boolean) value);

		} else if (value instanceof ArrayList) {
			List<FieldInfo> fields = fieldInfo.getStruct().getFields();
			json.writeObjectFieldStart(fieldName);
			writeArrayField(json, fields, field);
			json.writeEndObject();
		} else {
			throw new DaoException("Field type not supported: " + value.getClass().getName());
		}
		
	}
	
	private void writeArrayField(JsonGenerator json, List<FieldInfo> fieldInfo, FieldValue field) throws IOException, DaoException {
		@SuppressWarnings("unchecked")
		ArrayList<FieldValue> listOfValues = (ArrayList<FieldValue>) field.getValue();
		for (int i = 0; i < listOfValues.size(); i++) {
			FieldValue _value = listOfValues.get(i);
			writeField(json,fieldInfo.get(i),_value);			
		}
	}
	protected BigQuery getBigQuery() {
		return bigQuery;
	}

	@Override
	ChartSeriesFactory getChartSeriesFactory() {
		return new BigQueryChartSeriesFactory(bigQuery);
	}

}
