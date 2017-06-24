package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Common
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;

public class BigQueryDataFileTest {

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws Exception {
		File file = new File("src/test/resources/BigQueryDataFileTest/kitchenSink.jsonl");
		try(
				BigQueryDataFile dataFile = BigQueryDataFile.jsonFile(file)
		) {
			Iterator<InsertAllRequest.RowToInsert> sequence = dataFile.iterable().iterator();
			assertTrue(sequence.hasNext());
			RowToInsert firstRow = sequence.next();
			Map<String,Object> map = firstRow.getContent();
			assertEquals(1.23, map.get("floatValue"));
			assertEquals(102l, map.get("intValue"));
			assertEquals("Hello World!", map.get("stringValue"));
			Map<String,Object> record = (Map<String,Object>) map.get("recordValue");
			assertEquals("happy", record.get("mood"));
			List<Map<String,Object>> list = (List<Map<String,Object>>) record.get("repeatedValue");
			assertEquals(2, list.size());
			Map<String,Object> element = list.get(0);
			assertEquals("foo", element.get("msg"));
			
			element = list.get(1);
			assertEquals("bar", element.get("msg"));
			
			assertTrue(sequence.hasNext());
			RowToInsert secondRow = sequence.next();
			map = secondRow.getContent();
			assertEquals("Goodbye", map.get("stringValue"));
			assertTrue(!sequence.hasNext());
		}
	}

}
