package io.konig.sql.runtime;

import com.google.cloud.bigquery.BigQuery;

public class MockBigQueryShapeReadService extends BigQueryShapeReadService {

	BigQuery bigQuery;
	
	public MockBigQueryShapeReadService(BigQuery bigQuery, TableStructureService structService) {
		super(structService);
		this.bigQuery = bigQuery;
	}

	protected BigQuery getBigQuery() {
		return bigQuery;
	}

}
