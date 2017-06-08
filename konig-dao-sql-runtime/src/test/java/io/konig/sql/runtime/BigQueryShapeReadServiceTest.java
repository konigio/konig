package io.konig.sql.runtime;

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
		
		TableStructure struct = new TableStructure("schema.Person");
		struct.addField("givenName");
		TableStructureService structService = mock(TableStructureService.class);
		when(structService.tableStructureForShape(anyString())).thenReturn(struct);
		
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
		
		
		MockBigQueryShapeReadService service = new MockBigQueryShapeReadService(bigQuery, structService);
		
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
