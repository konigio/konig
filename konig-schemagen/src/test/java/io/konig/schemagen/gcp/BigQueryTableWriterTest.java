package io.konig.schemagen.gcp;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;

public class BigQueryTableWriterTest {

	@Test
	public void test() throws Exception {

		File outDir = new File("target/test/tables");
		File tableFile = new File(outDir, "myproject.mydataset.Person");
		
		if (tableFile.exists()) {
			tableFile.delete();
		}
		
		URI shapeId = uri("http://example.com/shape/schema/Person");
		
		GoogleCloudProject project = new GoogleCloudProject();
		project.setProjectId("myproject");
		
		BigQueryDataset dataset = new BigQueryDataset();
		dataset.setDatasetId("mydataset");
		project.addProjectDataset(dataset);
		
		BigQueryTable table = new BigQueryTable();
		table.setTableDataset(dataset);
		table.setTableId("Person");
		table.setTableShape(shapeId);
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		shapeBuilder.beginShape(shapeId)
			.targetClass(Schema.Person)
			.beginProperty(Schema.givenName)
				.datatype(XMLSchema.STRING)
				.minCount(1)
				.maxCount(1)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		
		BigQueryTableGenerator generator = new BigQueryTableGenerator()
			.setShapeManager(shapeManager);
		
		BigQueryTableWriter tableWriter = new BigQueryTableWriter(outDir, generator);
		
		tableWriter.add(table);
		
		
		assertTrue(tableFile.exists());
		
		Table loadedTable = loadTable(tableFile);
		
		assertEquals("Person", loadedTable.getTableReference().getTableId());
		assertEquals("mydataset", loadedTable.getTableReference().getDatasetId());
		assertEquals("myproject", loadedTable.getTableReference().getProjectId());
		
		TableSchema schema = loadedTable.getSchema();
		
		assertField(schema, "givenName", "REQUIRED", "STRING");
		
		
	
	}
	
	private void assertField(TableSchema schema, String fieldName, String mode, String type) {

		List<TableFieldSchema> fieldList = schema.getFields();
		for (TableFieldSchema field : fieldList) {
			if (field.getName().equals(fieldName)) {
				assertEquals(mode, field.getMode());
				assertEquals(type, field.getType());
				return;
			}
		}
		
		fail("Field not found: " + fieldName);
		
	}

	private Table loadTable(File tableFile) throws Exception {
		
		Reader reader = new FileReader(tableFile);
		
		JacksonFactory factory = JacksonFactory.getDefaultInstance();
		return factory.createJsonObjectParser().parseAndClose(reader, Table.class);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
