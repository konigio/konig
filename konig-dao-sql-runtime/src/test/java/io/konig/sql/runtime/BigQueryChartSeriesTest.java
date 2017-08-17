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


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.openrdf.model.vocabulary.XMLSchema;

import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryResult;

import io.konig.dao.core.OrderedPair;

public class BigQueryChartSeriesTest {

	@Test
	public void test() {
		
		String title = "foo";
		QueryResult queryResult = mock(QueryResult.class);
		FieldInfo dimension = new FieldInfo("intervalStart");
		dimension.setFieldType(XMLSchema.DATETIME);
		FieldInfo measure = new FieldInfo("count");
		measure.setFieldType(XMLSchema.INT);
		
		
		List<FieldValue> record = new ArrayList<>();
		appendFieldValue(record, "2017-08-17T16:18:08Z", 123);
		List<List<FieldValue>> results = new ArrayList<>();
		results.add(record);
		
		when(queryResult.iterateAll()).thenReturn(results);
		
		Iterable<List<FieldValue>> iter = queryResult.iterateAll();
		assertEquals(results, iter);
		
		BigQueryChartSeries series = new BigQueryChartSeries(title, queryResult, dimension, measure);
		
		Iterator<OrderedPair> sequence = series.iterator();
		assertTrue(sequence.hasNext());
		
		OrderedPair pair = sequence.next();
		
		Object x = pair.getX();
		Object y = pair.getY();
		
		assertTrue(x instanceof DateTime);
		
		long expected = ISODateTimeFormat.dateTimeParser().parseDateTime("2017-08-17T16:18:08Z").getMillis();
		assertEquals(expected, ((DateTime)x).getMillis());
		
		assertTrue(y instanceof Integer);
		assertEquals(new Integer(123), y);
	}

	private void appendFieldValue(List<FieldValue> record, String dimension, int measure) {
		
		long microSeconds = ISODateTimeFormat.dateTimeParser().parseDateTime(dimension).getMillis() * 1000;
		FieldValue timeValue = mock(FieldValue.class);
		when(timeValue.getTimestampValue()).thenReturn(microSeconds);
		
		FieldValue countValue = mock(FieldValue.class);
		when(countValue.getLongValue()).thenReturn((long) measure);
		
		record.add(timeValue);
		record.add(countValue);
		
	}

}
