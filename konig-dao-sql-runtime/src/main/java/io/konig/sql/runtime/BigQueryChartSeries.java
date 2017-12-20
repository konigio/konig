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


import java.util.Iterator;
import java.util.List;

import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryResult;

import io.konig.dao.core.AbstractChartSeries;
import io.konig.dao.core.OrderedPair;

public class BigQueryChartSeries extends AbstractChartSeries {
	private QueryResult result;
	private FieldInfo measure;
	private FieldInfo dimension;
	private String nextPageToken;
	
	/**
	 * Create a BigQueryChartSeries
	 * 
	 * @param title The title of the series (may be null).
	 * 
	 * @param result The BigQuery result. Each record in the result 
	 * 	set must contain a list of field values where the first value 
	 * is the dimension and the second value is the measure.
	 * 
	 * @param dimension Information about the dimension
	 * @param measure Information about the measure
	 */
	public BigQueryChartSeries(String title, QueryResult result, FieldInfo dimension, FieldInfo measure) {
		super(title);
		this.result = result;
		this.measure = measure;
		this.dimension = dimension;
		this.nextPageToken = result.getNextPageToken();
	}
	
	public String getNextPageToken() {
		return this.nextPageToken == null ? "" : this.nextPageToken;
	}
	
	@Override
	public Iterator<OrderedPair> iterator() {
		if(result.hasNextPage()) {
			return new QueryResultIterator(result.getValues().iterator());
		} 
		return new QueryResultIterator(result.iterateAll().iterator());
	}
	
	class QueryResultIterator implements Iterator<OrderedPair> {
		private Iterator<List<FieldValue>> source;
		

		public QueryResultIterator(Iterator<List<FieldValue>> source) {
			this.source = source;
		}

		@Override
		public boolean hasNext() {
			return source.hasNext();
		}

		@Override
		public OrderedPair next() {
			List<FieldValue> list = source.next();
			Object x=BigQueryUtil.dataValue(list.get(0), dimension.getFieldType().stringValue());
			Object y=BigQueryUtil.dataValue(list.get(1), measure.getFieldType().stringValue());
			
			return new OrderedPair(x, y);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
