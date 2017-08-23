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
import java.util.Map;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryRequest.Builder;

import io.konig.dao.core.DaoException;

import com.google.cloud.bigquery.QueryResponse;

public class BigQueryDataTransformService {

	private BigQuery bigQuery;

	public BigQueryDataTransformService(BigQuery bigQuery) {
		this.bigQuery = bigQuery;
	}

	public void executeSql(String queryString, HashMap<String, String> queryParams) throws DaoException {
		Builder builder = QueryRequest.newBuilder(queryString);
		for (Map.Entry<String, String> entry : queryParams.entrySet()) {
			builder.addNamedParameter(entry.getKey(), QueryParameterValue.string(entry.getValue()));
		}
		QueryRequest queryRequest = builder.setUseLegacySql(false).build();	
		QueryResponse response = bigQuery.query(queryRequest);
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
	}
}
