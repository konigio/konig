package io.konig.core.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;

public class MemoryGraphTest  {
	
	@Test
	public void testCreateVertex() {
		Graph graph = new MemoryGraph();
		Vertex v = graph.vertex("http://example.com/Alice");
		
		Vertex w = graph.getVertex(v.getId());
		
		assertEquals(v.getId(), w.getId());
	}
	
	@Test
	public void testCreateEdge() {
		Graph graph = new MemoryGraph();
		
		Vertex alice = graph.vertex("http://example.com/Alice");
		Vertex bob = graph.vertex("http://example.com/Bob");
		
		URI likes = new URIImpl("http://example.com/likes");
		
		Edge e = graph.edge(alice, likes, bob);
		
		Set<Edge> out = alice.outProperty(likes);
		
		assertEquals(out.size(), 1);
		assertEquals(out.iterator().next(), e);
		
		Set<Edge> in = bob.inProperty(likes);
		assertEquals(in.size(), 1);
	}

}
