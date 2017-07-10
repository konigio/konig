package io.konig.data.app.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Schema;
import io.konig.data.app.common.BasicDataApp;
import io.konig.data.app.common.ExtentContainer;
import io.konig.openapi.model.OpenAPI;
import io.konig.shacl.MediaTypeManager;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.SimpleMediaTypeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.yaml.Yaml;

public class DataAppGeneratorTest {
	
	

	@Test
	public void test() throws Exception {
		OpenAPI api = loadOpenApi("src/test/resources/gcp-data-services/openapi.yaml");
		
		DataAppGenerator generator = createGenerator("src/test/resources/gcp-data-services/rdf");
		
		BasicDataApp app = (BasicDataApp) generator.toDataApp(api);
		
		assertTrue(app != null);
		
		ExtentContainer container = app.getContainerForSlug("person");
		assertTrue(container != null);
		assertEquals(Schema.Person, container.getExtentClass());
		assertEquals(uri("http://example.com/shapes/PersonShape"), container.getDefaultShape());
		assertEquals("person", container.getSlug());

		String text = Yaml.toString(app);
		System.out.println(text);
		
	
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private DataAppGenerator createGenerator(String path) throws Exception {

		File file = new File(path);
		Graph graph = new MemoryGraph();
		ShapeManager shapeManager = new MemoryShapeManager();
		RdfUtil.loadTurtle(file, graph, shapeManager);
		
		MediaTypeManager mediaTypeManager = new SimpleMediaTypeManager(shapeManager);
		
		return new DataAppGenerator(mediaTypeManager);
	}

	private OpenAPI loadOpenApi(String path) throws Exception {
		try (FileReader reader = new FileReader(path)) {
			
			return Yaml.read(OpenAPI.class, reader);
		}
	}

}
