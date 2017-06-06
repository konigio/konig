package io.konig.dao.sql.generator;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;

public class BasicSqlDataSourceProcessorTest {
	
	private BasicSqlDataSourceProcessor processor = new BasicSqlDataSourceProcessor("com.example.sql");

	@Test
	public void test() throws Exception {
		
		URI shapeId = uri("http://example.com/shape/PersonShape");
		URI tableId = uri("https://example.com/bigquery/PersonTable");
		
		
		Shape shape = new Shape(shapeId);
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setId(tableId);
		shape.addShapeDataSource(table);
		
		List<DataSource> dsList = processor.findDataSources(shape);
		assertEquals(1, dsList.size());
		
		DataSource ds = dsList.get(0);
		
		String actual = processor.shapeReaderClassName(shape, ds);
		
		String expected = "com.example.sql.PersonSqlReadService";
		
		assertEquals(expected, actual);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
