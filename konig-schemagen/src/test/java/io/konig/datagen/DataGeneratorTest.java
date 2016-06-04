package io.konig.datagen;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.KDG;
import io.konig.pojo.io.SimplePojoFactory;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeLoader;

public class DataGeneratorTest {

	@Test
	public void test() throws Exception {
		
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph();
		RdfUtil.loadTurtle(graph, resource("DataGeneratorTest.ttl"), "");
		Vertex v = graph.v(KDG.DataGeneratorConfig).in(RDF.TYPE).firstVertex();
		
		DataGeneratorConfig config = new SimplePojoFactory().create(v, DataGeneratorConfig.class);
		
		File outDir = new File("target/test/datagen/");
		
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		SimpleShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("shapes/Membership-x1.ttl"));
		shapeLoader.loadTurtle(resource("shapes/School-x1.ttl"));
		shapeLoader.loadTurtle(resource("shapes/CourseSection-x1.ttl"));
		
		DataGenerator generator = new DataGenerator(nsManager, shapeManager, mediaTypeNamer);
		generator.generate(config, outDir);
		
		
		fail("Not yet implemented");
	}
	
	private InputStream resource(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
