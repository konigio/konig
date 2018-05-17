package io.konig.schemagen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.FileSet;
import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.maven.ViewShapeGeneratorConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ViewShapeGeneratorTest {

	@Test
	public void test() throws Exception {
		AwsShapeConfig.init();
		ViewShapeGeneratorConfig config = new ViewShapeGeneratorConfig();
		config.setPropertyNamespace("http://schema.org/");
		config.setShapeIriPattern("(.*)View$");
		config.setShapeIriReplacement("http://example.com/shapes/$1Shape");
		FileSet[] viewFiles = new FileSet[1];
		FileSet fileset = new FileSet();
		fileset.setDirectory("src/test/resources/view-shape-generator/view");
		viewFiles[0] = fileset;
		config.setViewFiles(viewFiles);
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("shape", "http://example.com/shapes/");
		nsManager.add("sh", "http://www.w3.org/ns/shacl#");
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsManager.add("konig", "http://www.konig.io/ns/core/");
		nsManager.add("xsd", "http://www.w3.org/2001/XMLSchema#");
		
		ShapeManager shapeManager = loadShapes("view-shape-generator/shape_OriginAccountShape.ttl");
		File outDir = new File("target/test/view-shape-generator");
		ViewShapeGenerator shapeGenerator = new ViewShapeGenerator(nsManager, shapeManager, config);
		shapeGenerator.generate(outDir);
		
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
