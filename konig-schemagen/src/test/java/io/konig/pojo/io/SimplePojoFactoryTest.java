package io.konig.pojo.io;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.KDG;
import io.konig.datagen.DataGeneratorConfig;
import io.konig.datagen.ShapeConfig;

public class SimplePojoFactoryTest {

	@Test
	public void test() {
		
		URI targetShape = uri("http://example.com/shape/foo");
		MemoryGraph graph = new MemoryGraph();
	
		graph.builder().beginSubject()
			.addProperty(RDF.TYPE, KDG.DataGeneratorConfig)
			.beginBNode(KDG.generate)
				.addProperty(KDG.targetShape, targetShape)
				.addProperty(KDG.shapeCount, 5)
			.endSubject()
		.endSubject();
		
		Vertex v = graph.v(KDG.DataGeneratorConfig).in(RDF.TYPE).firstVertex();
		
		
		SimplePojoFactory factory = new SimplePojoFactory();
		
		DataGeneratorConfig config = factory.create(v, DataGeneratorConfig.class);
		
		List<ShapeConfig> list = config.getShapeConfigList();
		
		assertEquals(1, list.size());
		
		ShapeConfig shapeConfig = list.get(0);
		
		assertEquals(targetShape, shapeConfig.getTargetShape());
		assertEquals(new Integer(5), shapeConfig.getShapeCount());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
