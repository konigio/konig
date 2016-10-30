package io.konig.core.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;

public class MemoryGraphTest  {
	
	@Test
	public void testRemoveBNode() {
		
		URI aliceId = uri("http://example.com/Alice");
		
		MemoryGraph g = new MemoryGraph();
		g.builder()
			.beginSubject(aliceId)
				.beginBNode(Schema.address)
					.addLiteral(Schema.streetAddress, "101 Main Street")
				.endSubject()
			.endSubject();
		
		Vertex alice = g.getVertex(aliceId);
		Vertex address = alice.getVertex(Schema.address);
		g.remove(address);
		
		System.out.println(g.toString());
		
	}
	
	@Ignore
	public void testCreateVertex() {
		Graph graph = new MemoryGraph();
		Vertex v = graph.vertex("http://example.com/Alice");
		
		Vertex w = graph.getVertex(v.getId());
		
		assertEquals(v.getId(), w.getId());
	}
	
	@Ignore
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
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
