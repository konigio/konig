package io.konig;

/*
 * #%L
 * Camel GoogleBigquery Component
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


import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

/**
 * The GoogleBigquery producer.
 */
public class GoogleBigqueryProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleBigqueryProducer.class);
    private GoogleBigqueryEndpoint endpoint;
    private BigQuery bigQuery;
    
    public GoogleBigqueryProducer(GoogleBigqueryEndpoint endpoint, BigQuery bigQuery) {
        super(endpoint);
        this.endpoint = endpoint;
        this.bigQuery = bigQuery;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println("My Test Component BQ === " + endpoint.getProjectId() + 
        		endpoint.getTableId() +
        		endpoint.getDatasetId() + 
        		exchange.getIn().getBody());    
        
        String sql = (String) exchange.getIn().getBody();
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
			throw new Exception(firstError);
		}
		
		QueryResult result = response.getResult();
    }

}
