package io.konig.sql.runtime;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValue.Attribute;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.MockFieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

public class MockBigQueryBuilder {
	
	private BigQuery bigQuery;
	private QueryResponse response;
	private QueryResult result;
	private List<List<FieldValue>> rowList;
	private List<FieldValue> columnList;
	
	public MockBigQueryBuilder beginBigQuery() {
		bigQuery = mock(BigQuery.class);
		return this;
	}
	
	
	
	public MockBigQueryBuilder beginResponse() {

		response = mock(QueryResponse.class);
		result = mock(QueryResult.class);
		when(response.getResult()).thenReturn(result);
		
		rowList = new ArrayList<>();
		when(bigQuery.query((QueryRequest)anyObject())).thenReturn(response);
		
		when(response.jobCompleted()).thenReturn(false).thenReturn(true);
		
		when(response.getJobId()).thenReturn(null);
		when(bigQuery.getQueryResults((JobId) null)).thenReturn(response);

		
		return this;
	}
	
	public MockBigQueryBuilder beginRow() {
		columnList = new ArrayList<>();
		rowList.add(columnList);
		return this;
	}
	
	public MockBigQueryBuilder addValue(String value) {
		columnList.add(new MockFieldValue(Attribute.PRIMITIVE, value));
		return this;
	}
	
	public MockBigQueryBuilder endRow() {
		return this;
	}
	
	public MockBigQueryBuilder endResponse() {
		when(result.iterateAll()).thenReturn(rowList);
		return this;
	}
	
	public BigQuery build() {
		return bigQuery;
	}

}
