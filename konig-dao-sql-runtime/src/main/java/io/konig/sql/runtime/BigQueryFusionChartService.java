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


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import io.konig.dao.core.ChartGeoLocationMapping;
import io.konig.dao.core.DaoException;

public class BigQueryFusionChartService {
	private BigQuery bigQuery;
	
	public BigQueryFusionChartService(BigQuery bigQuery){
		this.bigQuery = bigQuery;	
	}
	
	public ChartGeoLocationMapping getFusionIdMapping() throws DaoException {
		String sql = "SELECT * FROM util.FusionMapping";
		QueryRequest request = QueryRequest.newBuilder(sql).setUseLegacySql(false).build();
		QueryResponse response = bigQuery.query(request);
		while (!response.jobCompleted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			response = bigQuery.getQueryResults(response.getJobId());
		}
		
		if (response.hasErrors()) {
			String firstError = "";
			if (response.getExecutionErrors().size() != 0) {
				firstError = response.getExecutionErrors().get(0).getMessage();
			}
			throw new DaoException(firstError);
		}
		
		QueryResult result = response.getResult();
		
		Iterator<List<FieldValue>> sequence = result.iterateAll().iterator();
		ChartGeoLocationMapping locationMapping = new ChartGeoLocationMapping();
		while (sequence.hasNext()) {
			List<FieldValue> row = sequence.next();
			locationMapping.put(row.get(0).getStringValue(), 
					new ChartGeoLocationMapping.FusionMapping(row.get(1).getStringValue(),
							row.get(2).getStringValue(),
							row.get(3).getStringValue(),
							row.get(4).getStringValue()
							));
		}
		return locationMapping;
	}	
}
