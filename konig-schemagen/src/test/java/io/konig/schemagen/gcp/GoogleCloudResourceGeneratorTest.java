package io.konig.schemagen.gcp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class GoogleCloudResourceGeneratorTest {
	

	@Test
	public void testBigQueryTable() throws Exception {
		
		ShapeManager shapeManager = loadShapes("GoogleCloudResourceGeneratorTest/testBigQueryTable.ttl");
		File outDir = new File("target/test/GoogleCloudResourceGeneratorTest");

		File expectedFile = new File(outDir, "schema.Person.json" );
		expectedFile.delete();
		
		GoogleCloudResourceGenerator generator = new GoogleCloudResourceGenerator();
		generator.generateBigQueryTables(shapeManager.listShapes(), outDir);
		
		assertTrue(expectedFile.exists());
	}

	private ShapeManager loadShapes(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = loadGraph(resource);
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		
		return shapeManager;
	}

	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph();
		InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			RdfUtil.loadTurtle(graph, stream, "");
		} finally {
			IOUtil.close(stream, resource);
		}
		return graph;
	}

}
