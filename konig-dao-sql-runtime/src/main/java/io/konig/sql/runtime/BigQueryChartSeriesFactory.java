package io.konig.sql.runtime;

import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.memcache.MemcacheServiceFactory;

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


import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.QueryResultsOption;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import com.google.cloud.bigquery.QueryRequest.Builder;
import io.konig.dao.core.ChartGeoLocationMapping;
import io.konig.dao.core.ChartSeries;
import io.konig.dao.core.ChartSeriesFactory;
import io.konig.dao.core.ChartSeriesRequest;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.DataFilter;
import io.konig.dao.core.FieldPath;
import io.konig.dao.core.ShapeQuery;

public class BigQueryChartSeriesFactory extends SqlGenerator implements ChartSeriesFactory {
	
	private BigQuery bigQuery;
	private ChartGeoLocationMapping mapping = null;

	public BigQueryChartSeriesFactory(BigQuery bigQuery) {
		this.bigQuery = bigQuery;		
	}


	@Override
	public ChartSeries createChartSeries(ChartSeriesRequest seriesRequest) throws DaoException {
		 mapping = (ChartGeoLocationMapping)MemcacheServiceFactory
				.getMemcacheService()
				.get("FusionIdMapping");
		if(mapping == null){			
			ChartGeoLocationMapping mapping = new BigQueryFusionChartService(bigQuery , seriesRequest.getStruct().getMediaTypeBaseName())
					.getFusionIdMapping();
			MemcacheServiceFactory.getMemcacheService().put("FusionIdMapping", mapping);
			
		}
		EntityStructure struct=seriesRequest.getStruct();
		FieldPath dimension = seriesRequest.getDimension();
		FieldPath measure = seriesRequest.getMeasure();
		ShapeQuery query = seriesRequest.getQuery();
		String sql = sql(query, struct, dimension, measure);


		Builder querybuilder =  QueryRequest.newBuilder(sql).setUseLegacySql(false);
		if(query.getCursor() != null) {
			if(query.getLimit()!= null) {
				querybuilder.setPageSize(query.getLimit());
			}else {
				querybuilder.setPageSize(1000L);
			}
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
			response = bigQuery.getQueryResults(response.getJobId(), QueryResultsOption.pageToken(query.getCursor()));
		}
		if (response.hasErrors()) {
			String firstError = "";
			if (response.getExecutionErrors().size() != 0) {
				firstError = response.getExecutionErrors().get(0).getMessage();
			}
			throw new DaoException(firstError);
		}
		// TODO: look for execution errors
		QueryResult result = response.getResult();
		String title = struct.getComment();
		return new BigQueryChartSeries(title, result, dimension.lastField(), measure.lastField());
	}


	protected String sql(ShapeQuery query, EntityStructure struct, FieldPath dimension, FieldPath measure) {
		StringBuilder builder = new StringBuilder();
		DataFilter filter = query.getFilter();
		if(query.getAggregate()!= null && !query.getAggregate().equals("")) {
			builder.append("SELECT ");
			appendColumns(struct,builder,dimension);
			builder.append(", ");
			builder.append(query.getAggregate());
			builder.append("("+measure.stringValue()+")");
			builder.append(" FROM ");
			builder.append(struct.getName());
			appendFilter(struct,builder, filter);
			appendGroupBy(struct,builder,dimension);
		} else {
			builder.append("SELECT ");
			builder.append(dimension.stringValue());
			builder.append(", ");
			builder.append(measure.stringValue());
			builder.append(" FROM ");
			builder.append(struct.getName());
			appendFilter(struct,builder, filter);
		}
		if(query.getXSort() != null || query.getYSort() != null) {
			builder.append(" ORDER BY ");
			if(query.getXSort() != null && !query.getXSort().equals("")) {
				builder.append(dimension.stringValue());
				builder.append(" ");
				builder.append(query.getXSort());
				if(checkNullAndEmpty(query.getYSort())) {
					builder.append(",");
				}
			}
			if(checkNullAndEmpty(query.getYSort())) {
				builder.append(measure.stringValue());
				builder.append(" ");
				builder.append(query.getYSort());
			}
		}
		
		if(query.getCursor() == null) {
			if(checkNullAndEmpty(query.getLimit())) {
				builder.append(" LIMIT " + query.getLimit());
				if(query.getOffset() != null && !query.getOffset().equals("")) {
					builder.append(" OFFSET " + query.getOffset());
				}
				
			}
		}
		return builder.toString();
	}
	
	private boolean checkNullAndEmpty(Object value) {
		if(value != null && value.equals("")) {
			return true;
		}
		return false;
	}
}
