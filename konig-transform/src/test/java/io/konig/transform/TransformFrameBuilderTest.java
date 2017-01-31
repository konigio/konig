package io.konig.transform;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.PathFactory;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class TransformFrameBuilderTest {
	
	private ShapeManager shapeManager = new MemoryShapeManager();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private PathFactory pathFactory = new PathFactory(nsManager, graph);

	@Test
	public void testIriReference() throws Exception {
		
		loadGraph("TransformFrameBuilderTest/testIriReference.ttl");
		
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/warehouse/ProductShape"));
		Shape sourceShape = shapeManager.getShapeById(uri("http://example.com/shapes/origin/ProductShape"));
		
		assertTrue(targetShape!=null);
		assertTrue(sourceShape != null);

		TransformFrameBuilder frameBuilder = new TransformFrameBuilder(shapeManager, pathFactory);
		TransformFrame frame = frameBuilder.create(targetShape);
		
//		System.out.println(frame);
		
		Collection<TransformAttribute> attributes = frame.getAttributes();
		
		assertEquals(2, attributes.size());
		
		TransformAttribute acmeIdAttr = frame.getAttribute(uri("http://example.com/ns/acme/acmeId"));
		assertTrue(acmeIdAttr != null);
		
		MappedProperty p = acmeIdAttr.getMappedProperty(sourceShape);
		assertTrue(p != null);
		
		assertEquals(p.getProperty().getPredicate(), uri("http://example.com/ns/alias/product_id"));
		assertEquals(0, p.getStepIndex());
		
		TransformAttribute brandAttr = frame.getAttribute(Schema.brand);
		assertTrue(brandAttr != null);
		
		p = brandAttr.getMappedProperty(sourceShape);
		assertTrue(p != null);
		
		assertEquals(p.getProperty().getPredicate(), uri("http://example.com/ns/alias/vendor_id"));
		IriTemplateInfo template = p.getTemplateInfo();
		assertTrue(template != null);
		
		MappedId mappedId = frame.getIdMapping(sourceShape);
		assertTrue(mappedId != null);
		
		
		
	}
	
	private void loadGraph(String filePath) throws Exception {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(filePath);
		if (input == null) {
			throw new Exception("Resource not found: " + filePath);
		}
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
