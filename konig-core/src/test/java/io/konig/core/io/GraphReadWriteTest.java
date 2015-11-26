package io.konig.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import io.konig.core.Context;
import io.konig.core.ContextBuilder;
import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.KonigTest;
import io.konig.core.Traversal;
import io.konig.core.impl.ContextManagerImpl;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.TraversalImpl;
import io.konig.core.vocab.Schema;

public class GraphReadWriteTest extends KonigTest {

	@Test
	public void testEmptyContext() throws Exception {

		URI alice = uri("http://example.com/alice");
		URI bob = uri("http://example.com/bob");
		URI likes = uri("http://example.com/likes");
		
		Context context = new Context("http://example.com/context");
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		
		
		Graph graph = new MemoryGraph();
		graph.edge(alice, likes, bob);
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		Graph graph2 = new MemoryGraph();
		buffer.readGraph(data, graph2, manager);
		
		Traversal g = graph2.v(alice).hasValue(likes, bob);
		
		assertEquals(1, g.size());
	}
	
	@Test
	public void testPropertyTerm() throws Exception {


		URI alice = uri("http://example.com/alice");
		URI bob = uri("http://example.com/bob");
		URI likes = uri("http://example.com/likes");
		
		
		Context context = new ContextBuilder("http://example.com/context")
			.objectProperty("likes", "http://example.com/likes")
			.getContext();
		
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		
		
		Graph graph = new MemoryGraph();
		graph.edge(alice, likes, bob);
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		Graph graph2 = new MemoryGraph();
		buffer.readGraph(data, graph2, manager);
		
		Traversal g = graph2.v(alice).hasValue(likes, bob);
		
		assertEquals(1, g.size());
	}
	
	@Test
	public void testSubjectQName() throws Exception {

		URI alice = uri("http://example.com/alice");
		Literal aliceName = literal("Alice");
		
		
		Context context = new ContextBuilder("http://example.com/context")
			.namespace("ex", "http://example.com/")
			.objectProperty("givenName", "http://schema.org/givenName")
			.getContext();
		
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		
		
		Graph graph = new MemoryGraph();
		graph.edge(alice, Schema.givenName, aliceName);
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		Graph graph2 = new MemoryGraph();
		buffer.readGraph(data, graph2, manager);
		
		Traversal g = graph2.v(alice).hasValue(Schema.givenName, "Alice");
		
		assertEquals(1, g.size());
	}
	
	
	@Test
	public void testTwoResources() throws Exception {
		Context context = personContext().namespace("ex", "http://example.com/").getContext();
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		

		URI alice = uri("http://example.com/alice");
		URI bob = uri("http://example.com/bob");
		
		
		Graph graph = new MemoryGraph();
		Traversal t = new TraversalImpl(graph);
		
		t.addV(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addLiteral(Schema.familyName, "Jones")
			.addV(bob)
			.addLiteral(Schema.givenName, "Bob")
			.addLiteral(Schema.familyName, "Smith");
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		Graph g = new MemoryGraph();
		
		buffer.readGraph(data, g, manager);
		
		t = g.v(alice).hasValue(Schema.givenName, "Alice");
		
		assertTrue(g.v(alice).hasValue(Schema.givenName, "Alice").size()==1);
		assertTrue(g.v(alice).hasValue(Schema.familyName, "Jones").size()==1);
		assertTrue(g.v(bob).hasValue(Schema.givenName,  "Bob").size()==1);
		assertTrue(g.v(bob).hasValue(Schema.familyName, "Smith").size()==1);
		
	}
	

	@Test
	public void testInverseAttribute() throws Exception {
		Context context = personContext().namespace("ex", "http://example.com/").getContext();
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		

		URI alice = uri("http://example.com/alice");
		URI carl = uri("http://example.com/carl");
		
		
		Graph graph = new MemoryGraph();
		Traversal t = new TraversalImpl(graph);
		
		t.addV(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addProperty(Schema.parent, carl)
			.addV(carl)
			.addLiteral(Schema.givenName, "Carl")
			.addProperty(Schema.children, alice);
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		Graph g = new MemoryGraph();
		
		buffer.readGraph(data, g, manager);
		
		t = g.v(alice).hasValue(Schema.givenName, "Alice");
		
		assertTrue(g.v(alice).hasValue(Schema.givenName, "Alice").size()==1);
		assertTrue(g.v(alice).hasValue(Schema.parent, carl).size()==1);
		assertTrue(g.v(carl).hasValue(Schema.givenName,  "Carl").size()==1);
		assertTrue(g.v(carl).hasValue(Schema.children, alice).size()==1);
		
			
		
	}
	
	@Test
	public void testBNode() throws Exception {

		Context context = personContext().namespace("ex", "http://example.com/").getContext();
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		

		
		URI alice = uri("http://example.com/alice");
		BNode address = bnode();
		
		
		Graph graph = new MemoryGraph();
		Traversal t = new TraversalImpl(graph);
		
		t.addV(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addProperty(Schema.address, address)
			.addV(address)
			.addLiteral(Schema.streetAddress, "101 Main St")
			.addLiteral(Schema.addressLocality, "Springfield")
			.addLiteral(Schema.addressRegion, "VA");
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		Graph g = new MemoryGraph();
		
		buffer.readGraph(data, g, manager);
		
		Traversal aliceT = g.v(alice);
		Traversal addr = aliceT.out(Schema.address);
		
		assertTrue(aliceT.hasValue(Schema.givenName, "Alice").size()==1);
		assertTrue(addr.hasValue(Schema.streetAddress, "101 Main St").size()==1);
		assertTrue(addr.hasValue(Schema.addressLocality,  "Springfield").size()==1);
		assertTrue(addr.hasValue(Schema.addressRegion, "VA").size()==1);
	}
	
	
	@Test
	public void testJsonWriter() throws Exception {

		Context context = personContext().namespace("ex", "http://example.com/").getContext();
		ContextManager manager = new ContextManagerImpl();
		manager.add(context);
		

		
		URI alice = uri("http://example.com/alice");
		BNode address = bnode();
		
		
		Graph graph = new MemoryGraph();
		Traversal t = new TraversalImpl(graph);
		
		t.addV(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addProperty(Schema.address, address)
			.addV(address)
			.addLiteral(Schema.streetAddress, "101 Main St")
			.addLiteral(Schema.addressLocality, "Springfield")
			.addLiteral(Schema.addressRegion, "VA");
		
		GraphBuffer buffer = new GraphBuffer();
		byte[] data = buffer.writeGraph(graph, context);
		
		
		StringWriter writer = new StringWriter();
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(writer);
		json.setPrettyPrinter(new DefaultPrettyPrinter());
		
		buffer.writeJSON(data, manager, json);
		
		json.flush();
	}

}
