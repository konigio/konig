package io.konig.shacl;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.GraphBuilder;
import io.konig.core.KonigTest;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.GraphFilter;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class GraphFilterTest extends KonigTest {

	@Test
	public void testMinMaxCount() {
		
		Graph graph = new MemoryGraph();
		
		URI bob = uri("http://example.com/bob");
		URI alice = uri("http://example.com/alice");
		
		GraphBuilder builder = new GraphBuilder(graph);
		builder
			.literalProperty(bob, Schema.givenName, "Robert")
			.literalProperty(bob, Schema.givenName, "Bob")
			.literalProperty(alice, Schema.givenName, "Alice");
		
		
		Shape shape = new ShapeBuilder()
			.beginShape()
				.property(Schema.givenName)
					.minCount(1)
					.maxCount(1)
			.shape();
		
		
		Vertex aliceVertex = graph.vertex(alice);
		Vertex bobVertex = graph.vertex(bob);
		
		GraphFilter filter = GraphFilter.INSTANCE;
		
		assertTrue(filter.matches(aliceVertex, shape));
		assertTrue(!filter.matches(bobVertex, shape));
		
		
	}
	
	@Test
	public void testOrConstraint() {
		
		Graph graph = new MemoryGraph();
		
		URI bob = uri("http://example.com/bob");
		URI alice = uri("http://example.com/alice");
		URI carl = uri("http://example.com/carl");
		
		GraphBuilder builder = new GraphBuilder(graph);
		builder
			.literalProperty(bob, Schema.givenName, "Bob")
			.literalProperty(alice, Schema.givenName, "Alice")
			.literalProperty(alice, Schema.familyName, "Smith")
			.literalProperty(carl, Schema.name, "Carl Jones");

 		Shape  shape = new ShapeBuilder()
 			.beginShape()
	 			.beginOr()
	 				.beginShape()
	 					.property(Schema.name)
		 					.minCount(1)
		 					.maxCount(1)
	 				.endShape()
	 				.beginShape()
	 					.property(Schema.givenName)
	 						.minCount(1)
	 						.maxCount(1)
	 					.property(Schema.familyName)
	 						.minCount(1)
	 						.maxCount(1)
	 				.endShape()
	 			.endOr()
 			.shape();

		Vertex aliceVertex = graph.vertex(alice);
		Vertex bobVertex = graph.vertex(bob);
		Vertex carlVertex = graph.vertex(carl);

		GraphFilter filter = GraphFilter.INSTANCE;
		
		assertTrue(filter.matches(aliceVertex, shape));
		assertTrue(!filter.matches(bobVertex, shape));
		assertTrue(filter.matches(carlVertex, shape));
		
		
	}

}
