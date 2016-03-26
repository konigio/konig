package io.konig.services.impl;

/*
 * #%L
 * Konig Services
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.GraphBuilder;
import io.konig.core.Vertex;
import io.konig.core.impl.EdgeImpl;
import io.konig.core.impl.KonigLiteral;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;

public class BNodeMapperTest {

	@Test
	public void test() {
		
		Graph xGraph = new MemoryGraph();
		Graph yGraph = new MemoryGraph();
		
		new GraphBuilder(yGraph)
			.beginSubject("http://example.com/alice")
				.addLiteral(Schema.givenName, "Alice")
				.beginBNode(Schema.contactPoint)
					.addLiteral(Schema.contactType, "Personal")
					.addLiteral(Schema.email, "alice@example.com")
					.endSubject()
			.beginSubject("http://example.com/bob")
				.addLiteral(Schema.givenName, "Bob")
				.beginBNode(Schema.contactPoint)
					.addLiteral(Schema.contactType, "Personal")
					.addLiteral(Schema.email, "bob@example.com")
					.endSubject()
				.beginBNode(Schema.contactPoint)
					.addLiteral(Schema.contactType, "Work")
					.addLiteral(Schema.email, "bob@acme.com")
					.endSubject()
		;
		
		new GraphBuilder(xGraph)
		.beginSubject("http://example.com/alice")
			.beginBNode(Schema.contactPoint)
				.addLiteral(Schema.contactType, "Personal")
				.endSubject()
		.beginSubject("http://example.com/bob")
			.beginBNode(Schema.contactPoint)
				.addLiteral(Schema.contactType, "Personal")
				.endSubject()
			.beginBNode(Schema.contactPoint)
				.addLiteral(Schema.contactType, "Work")
				.endSubject()
		
		;
		
		BNodeMapper mapper = new BNodeMapper();
		
		Map<Vertex,Vertex> map = mapper.scan(xGraph, yGraph);
		
		Vertex aliceX = xGraph.getVertex("http://example.com/alice");
		Vertex aliceY = yGraph.getVertex("http://example.com/alice");
		
		Vertex bobX = xGraph.getVertex("http://example.com/bob");
		Vertex bobY = yGraph.getVertex("http://example.com/bob");
		
		Vertex alicePersonalX = aliceX.asTraversal().firstVertex(Schema.contactPoint);
		Vertex alicePersonalY = aliceY.asTraversal().firstVertex(Schema.contactPoint);
		
		Vertex bobPersonalX = bobX.asTraversal().out(Schema.contactPoint).hasValue(Schema.contactType, "Personal").firstVertex();
		Vertex bobPersonalY = bobY.asTraversal().out(Schema.contactPoint).hasValue(Schema.contactType, "Personal").firstVertex();
		
		Vertex bobWorkX = bobX.asTraversal().out(Schema.contactPoint).hasValue(Schema.contactType, "Work").firstVertex();
		Vertex bobWorkY = bobY.asTraversal().out(Schema.contactPoint).hasValue(Schema.contactType, "Work").firstVertex();
		
		assertEquals(alicePersonalY, map.get(alicePersonalX));
		assertEquals(bobPersonalY, map.get(bobPersonalX));
		assertEquals(bobWorkY, map.get(bobWorkX));
		
	}
	
	private Vertex bnode(Graph g, String label) {
		Vertex v = g.vertex();
		g.add(new EdgeImpl(v.getId(), RDFS.LABEL, new KonigLiteral(label)));
		return v;
	}
	

	@Test
	public void planarTest() {
		
		Graph x = new MemoryGraph();
		Graph y = new MemoryGraph();
		
		Vertex ax = x.vertex("http://example.com");
		Vertex bx = bnode(x, "b");
		Vertex cx = bnode(x, "c");
		Vertex dx = bnode(x, "d");
		Vertex ex = bnode(x, "e");
		
		Vertex ay = y.vertex("http://example.com");
		Vertex by = bnode(y, "b");
		Vertex cy = bnode(y, "c");
		Vertex dy = bnode(y, "d");
		Vertex ey = bnode(y, "e");
		
		new GraphBuilder(x)
			.beginSubject(ax)
				.addProperty(Schema.hasPart, bx)
				.addProperty(Schema.hasPart, dx)
				.endSubject()
			.beginSubject(bx)
				.addProperty(Schema.hasPart, cx)
				.addProperty(Schema.hasPart, ex)
				.endSubject()
			.beginSubject(cx)
				.addProperty(Schema.hasPart, dx)
				.endSubject()
			.beginSubject(dx)
				.addProperty(Schema.hasPart, ex)
				.endSubject()
			;
		

		new GraphBuilder(y)
			.beginSubject(ay)
				.addProperty(Schema.hasPart, by)
				.addProperty(Schema.hasPart, dy)
				.endSubject()
			.beginSubject(by)
				.addProperty(Schema.hasPart, cy)
				.addProperty(Schema.hasPart, ey)
				.endSubject()
			.beginSubject(cy)
				.addProperty(Schema.hasPart, dy)
				.endSubject()
			.beginSubject(dy)
				.addProperty(Schema.hasPart, ey)
				.endSubject()
			;
		

		
		
		BNodeMapper mapper = new BNodeMapper();
		
		Map<Vertex,Vertex> map = mapper.scan(x, y);

		assertEquals(by, map.get(bx));
		assertEquals(cy, map.get(cx));
		assertEquals(dy, map.get(dx));
		assertEquals(ey, map.get(ex));
		
	}
	
	
	private void printInEdges(Vertex v) {
		Graph graph = v.getGraph();
		
		for (Edge e : v.inEdgeSet()) {
			Resource subject = e.getSubject();
			URI predicate = e.getPredicate();
			Value object = e.getObject();
			
			Vertex x = graph.getVertex(subject);
			System.out.print(label(x));
			System.out.print(" ");
			System.out.print(predicate.getLocalName());
			System.out.print(" ");
			if (object instanceof Resource) {
				Vertex y = graph.getVertex((Resource) object);
				System.out.println(label(y));
			} else {
				System.out.print("literal=");
				System.out.println(object.stringValue());
			}
		}
		
	}

	private String label(Vertex v) {
		return v.asTraversal().firstValue(RDFS.LABEL).stringValue();
	}
	
	@Test
	public void nonPlanarGraphTest() {
		
		Graph x = new MemoryGraph();
		Graph y = new MemoryGraph();
		
		Vertex ax = x.vertex("http://example.com");
		Vertex bx = bnode(x, "b");
		Vertex cx = bnode(x, "c");
		Vertex dx = bnode(x, "d");
		Vertex ex = bnode(x, "e");
		Vertex fx = bnode(x, "f");
		
		Vertex ay = y.vertex("http://example.com");
		Vertex by = bnode(y, "b");
		Vertex cy = bnode(y, "c");
		Vertex dy = bnode(y, "d");
		Vertex ey = bnode(y, "e");
		Vertex fy = bnode(y, "f");
		
		new GraphBuilder(x)
			.beginSubject(ax)
				.addProperty(Schema.hasPart, bx)
				.addProperty(Schema.hasPart, dx)
				.addProperty(Schema.hasPart, fx)
				.endSubject()
			.beginSubject(bx)
				.addProperty(Schema.hasPart, cx)
				.addProperty(Schema.hasPart, fx)
				.addProperty(Schema.hasPart, ex)
				.endSubject()
			.beginSubject(cx)
				.addProperty(Schema.hasPart, fx)
				.addProperty(Schema.hasPart, dx)
				.endSubject()
			.beginSubject(dx)
				.addProperty(Schema.hasPart, ex)
				.endSubject()
			.beginSubject(ex)
				.addProperty(Schema.hasPart, fx)
				.endSubject()
			;
		

		new GraphBuilder(y)
			.beginSubject(ay)
				.addProperty(Schema.hasPart, by)
				.addProperty(Schema.hasPart, dy)
				.addProperty(Schema.hasPart, fy)
				.endSubject()
			.beginSubject(by)
				.addProperty(Schema.hasPart, cy)
				.addProperty(Schema.hasPart, fy)
				.addProperty(Schema.hasPart, ey)
				.endSubject()
			.beginSubject(cy)
				.addProperty(Schema.hasPart, fy)
				.addProperty(Schema.hasPart, dy)
				.endSubject()
			.beginSubject(dy)
				.addProperty(Schema.hasPart, ey)
				.endSubject()
			.beginSubject(ey)
				.addProperty(Schema.hasPart, fy)
				.endSubject()
			;
		

		BNodeMapper mapper = new BNodeMapper();
		
		Map<Vertex,Vertex> map = mapper.scan(x, y);

		assertEquals(by, map.get(bx));
		assertEquals(cy, map.get(cx));
		assertEquals(dy, map.get(dx));
		assertEquals(ey, map.get(ex));
		assertEquals(fy, map.get(fx));
		
	}
	

}
