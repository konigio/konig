package io.konig.sql.runtime;

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

	public BigQueryShapeReadService(TableNameService tableNameService) {
		super(tableNameService);
	}

	@Override
	protected void executeSql(String sql, Writer output, Format format) throws DaoException {
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
				writeJsonld(result, output);
				break;
			}
		} catch (IOException e) {
			throw new DaoException(e);
		}
		
	}
	
	private void writeJsonld(QueryResult result, Writer output) throws IOException, DaoException {
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(output);
		
		// TODO: Use Linked Data Platform conventions, not a top-level array.
		json.writeStartArray();
		
		Iterator<List<FieldValue>> sequence = result.iterateAll().iterator();
		while (sequence.hasNext()) {
			List<FieldValue> row = sequence.next();
			json.writeStartObject();
			for (FieldValue field : row) {
				writeField(json, field);
			}
			json.writeEndObject();
		}
		
		json.writeEndArray();
		
	}

	private void writeField(JsonGenerator json, FieldValue field) throws IOException, DaoException {
		
		String fieldName = field.getAttribute().name();
		Object value = field.getValue();
		
		if (value instanceof String) {
			json.writeStringField(fieldName, value.toString());
		} else {
			throw new DaoException("Field type not supported: " + value.getClass().getName());
		}
		
	}

	protected BigQuery getBigQuery() {
		return BigQueryOptions.getDefaultInstance().getService();
	}

}
