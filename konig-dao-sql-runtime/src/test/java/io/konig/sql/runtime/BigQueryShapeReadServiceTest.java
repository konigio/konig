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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;

public class BigQueryShapeReadServiceTest  {

	@Test
	public void test() throws Exception {
		
		String shapeId = "http://example.com/shape/PersonShape";
		
		EntityStructure struct = new EntityStructure("schema.Person");
		struct.addField("givenName");
		EntityStructureService structService = mock(EntityStructureService.class);
		when(structService.structureOfShape(anyString())).thenReturn(struct);
		
		BigQuery bigQuery = new MockBigQueryBuilder().beginBigQuery()
			.beginResponse()
				.beginRow()
					.addValue("Alice")
				.endRow()
			.endResponse()
		.build();
		
		QueryResponse response = mock(QueryResponse.class);
		QueryResult result = mock(QueryResult.class);
		when(response.getResult()).thenReturn(result);
		Iterable<List<FieldValue>> sequence = null;
		when(result.iterateAll()).thenReturn(sequence);
		
		
		BigQueryShapeReadService service = new BigQueryShapeReadService(structService, bigQuery);
		
		ShapeQuery query = new ShapeQuery.Builder()
				.setShapeId(shapeId)
				.beginPredicateConstraint()
					.setPropertyName("givenName")
					.setOperator(ConstraintOperator.EQUAL)
					.setValue("Alice")
				.endPredicateConstraint()
				.build();
			
		StringWriter buffer = new StringWriter();
		
		service.execute(query, buffer, Format.JSONLD);
		String expected = "[ {\n" + 
				"  \"givenName\" : \"Alice\"\n" + 
				"} ]";
		String actual = buffer.toString().replace("\r", "");
		
		assertEquals(expected, actual);
	}

}
