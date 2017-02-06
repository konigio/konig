package io.konig.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;

public class DataSourceGeneratorTest {

	@Test
	public void test() throws Exception {
		
		File templateDir = null;
		Graph graph = new MemoryGraph();
		
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("WorkbookLoader/settings.properties"));
		
		properties.put("gcpProjectId", "warehouse");
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);

		Shape shape = new Shape(uri("http://example.com/shapes/PersonOriginShape"));
		shape.setTargetClass(Schema.Person);
		
		DataSourceGenerator generator = new DataSourceGenerator(nsManager, templateDir, properties);
		generator.generate(shape, "BigQueryTable", graph);
		
		Vertex v = graph.v(Konig.GoogleBigQueryTable).in(RDF.TYPE).firstVertex();
		assertTrue(v != null);
		assertEquals(uri("https://www.googleapis.com/bigquery/v2/projects/warehouse/datasets/schema/tables/Person"), v.getId());
		
		
		Vertex tableRef = v.asTraversal().out(GCP.tableReference).firstVertex();
		assertTrue(tableRef != null);
		
		assertValue(tableRef, GCP.projectId, "warehouse");
		assertValue(tableRef, GCP.datasetId, "schema");
		assertValue(tableRef, GCP.tableId, "Person");
	}

	private void assertValue(Vertex v, URI predicate, String expected) {
		Value value = v.getValue(predicate);
		assertTrue(value != null);
		assertEquals(expected, value.stringValue());
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
