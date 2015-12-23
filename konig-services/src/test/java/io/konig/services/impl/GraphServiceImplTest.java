package io.konig.services.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Traversal;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.services.GraphService;
import io.konig.services.KonigServiceTest;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class GraphServiceImplTest extends KonigServiceTest {

	@Test
	public void testPerson() throws Exception {
		
		
		GraphService service = config().getGraphService();
		
		MemoryGraph graph = new MemoryGraph();
		
		URI alice = uri("http://example.com/alice");
		
		ShapeBuilder builder = new ShapeBuilder("http://www.konig.io/shape/schema/Person-v1");
		
		builder.property(Schema.givenName)
			.datatype(XMLSchema.STRING)
			.maxCount(1)
			.property(Schema.familyName)
			.maxCount(1);
		
		Shape personShape = builder.shape();
		
		
		graph.v(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addLiteral(Schema.familyName, "Smith");
		
		
		service.put(alice, personShape, graph);
		
		MemoryGraph sink = new MemoryGraph();
		service.get(alice, sink);
		
		Traversal aliceT = sink.v(alice);
		
		assertTrue(aliceT.hasValue(Schema.givenName, "Alice").size() == 1);
		assertTrue(aliceT.hasValue(Schema.familyName, "Smith").size() == 1);
		
		
	}

}
