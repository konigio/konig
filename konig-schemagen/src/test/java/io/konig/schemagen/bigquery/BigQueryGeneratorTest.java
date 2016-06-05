package io.konig.schemagen.bigquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.GCP;
import io.konig.pojo.io.PojoFactory;
import io.konig.pojo.io.SimplePojoFactory;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class BigQueryGeneratorTest {

	@Test
	public void testScan() throws Exception {
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		loadShape(shapeLoader, "src/test/resources/shapes/Membership-x1.ttl");
		loadShape(shapeLoader, "src/test/resources/shapes/Organization-x1.ttl");
		
		
		File sourceDir = new File("src/test/resources/bigquery");
		File outDir = new File("target/bigquery");
		outDir.mkdirs();
		BigQueryGenerator generator = new BigQueryGenerator(shapeManager);
		generator.writeTableDefinitions(sourceDir, outDir);
	}
	
	
	private void loadShape(ShapeLoader shapeLoader, String path) throws FileNotFoundException {
		InputStream stream = file(path);
		try {
			shapeLoader.loadTurtle(stream);
		} finally {
			close(stream);
		}
		
	}




	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (IOException e) {
		}
		
	}


	private InputStream file(String path) throws FileNotFoundException {
		File file = new File(path);
		return new FileInputStream(file);
	}


	@Ignore
	public void test() throws Exception {
		
		MemoryGraph graph = new MemoryGraph();
		RdfUtil.loadTurtle(graph, resource("bigquery/Organization-table.ttl"), "");
		PojoFactory factory = new SimplePojoFactory();
		
		Vertex v = graph.v(GCP.BigQueryTable).in(RDF.TYPE).firstVertex();
		BigQueryTable table = factory.create(v, BigQueryTable.class);
		
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter buffer = new StringWriter();
		JsonGenerator json = jsonFactory.createGenerator(buffer);
		json.useDefaultPrettyPrinter();
		
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("shapes/Organization-x1.ttl"));

		BigQueryGenerator generator = new BigQueryGenerator(shapeManager);
		generator.writeTableDefinition(table, json);
		json.flush();
		
		String text = buffer.toString();
		System.out.println(text);
		
		validateOrganizationTable(text, table);
		
	}

	private void validateOrganizationTable(String text, BigQueryTable table) throws JsonProcessingException, IOException {
		StringReader reader = new StringReader(text);
		BigQueryTableReference actualRef = table.getTableReference();
		
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode json = (ObjectNode) mapper.readTree(reader);
		
		ObjectNode ref = (ObjectNode) json.get("tableReference");
		assertTrue(ref != null);
		assertEquals(actualRef.getProjectId(), ref.get("projectId").asText());
		assertEquals(actualRef.getDatasetId(), ref.get("datasetId").asText());
		assertEquals(actualRef.getTableId(), ref.get("tableId").asText());
		assertEquals(table.getDescription(), json.get("description").asText());
		
	}

	private InputStream resource(String path) {
		
		return getClass().getClassLoader().getResourceAsStream(path);
	}
	

}
