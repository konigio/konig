package io.konig.core.path;

/*
 * #%L
 * Konig Core
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
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.Traverser;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class PathFactoryTest {
	
	@Test
	public void testBoolean() {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		
		URI one = uri("http://example.com/thing/one");
		URI two = uri("http://example.com/thing/two");
		URI three = uri("http://example.com/thing/three");
		
		URI isFamilyFriendly = uri("http://schema.org/isFamilyFriendly");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(one)
				.addProperty(RDF.TYPE, Schema.CreativeWork)
				.addProperty(isFamilyFriendly, true)
			.endSubject()
			.beginSubject(two)
				.addProperty(RDF.TYPE, Schema.CreativeWork)
				.addProperty(isFamilyFriendly, false)
			.endSubject()
			.beginSubject(three)
				.addProperty(RDF.TYPE, Schema.CreativeWork)
				.addProperty(isFamilyFriendly, true)
			.endSubject()
			;
		
		
		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath("schema:CreativeWork^rdf:type[schema:isFamilyFriendly true]");
		result = path.traverse(new Traverser(graph));
		
		assertEquals(2, result.size());
		assertTrue(result.contains(one));
		assertTrue(result.contains(three));
	}
	
	@Test
	public void testTypedString() {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		nsManager.add("xsd", XMLSchema.NAMESPACE);
		
		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		URI cathyId = uri("http://example.com/person/cathy");
		
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addTypedLiteral(Schema.birthDate, "1996-11-13", XMLSchema.DATE)
			.endSubject()
			.beginSubject(bobId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addTypedLiteral(Schema.birthDate, "1996-11-13", XMLSchema.DATE)
			.endSubject()
			.beginSubject(cathyId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addLiteral(Schema.birthDate, "1996-11-13")
			.endSubject()
			;
		
		
		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath("schema:Person^rdf:type[schema:birthDate \"1996-11-13\"^xsd:date]");
		result = path.traverse(new Traverser(graph));
		assertEquals(2, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
	}
	
	
	@Test
	public void testLangString() {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		
		URI one = uri("http://example.com/thing/one");
		URI two = uri("http://example.com/thing/two");
		URI three = uri("http://example.com/thing/three");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(one)
				.addProperty(RDF.TYPE, Schema.Thing)
				.addLangString(Schema.name, "fruit", "en")
			.endSubject()
			.beginSubject(two)
				.addProperty(RDF.TYPE, Schema.Thing)
				.addLangString(Schema.name, "fruit", "fr")
			.endSubject()
			.beginSubject(three)
				.addProperty(RDF.TYPE, Schema.Thing)
				.addLangString(Schema.name, "frucht", "de")
			.endSubject()
			;
		
		
		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath("schema:Thing^rdf:type[schema:name \"fruit\"@fr]");
		result = path.traverse(new Traverser(graph));
		assertEquals(1, result.size());
		assertTrue(result.contains(two));
	}
	
	@Test
	public void testString() {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		
		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		URI cathyId = uri("http://example.com/person/cathy");
		
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addLiteral(Schema.familyName, "Smith")
				.addProperty(Schema.gender, Schema.Female)
				.addProperty(Schema.parent, bobId)
				.addProperty(Schema.parent, cathyId)
			.endSubject()
			.beginSubject(bobId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addLiteral(Schema.familyName, "Smith")
				.addProperty(Schema.gender, Schema.Male)
			.endSubject()
			.beginSubject(cathyId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addLiteral(Schema.familyName, "Jones")
				.addProperty(Schema.gender, Schema.Female)
			.endSubject()
			;
		
		
		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath("schema:Person^rdf:type[schema:familyName \"Smith\"]");
		result = path.traverse(new Traverser(graph));
		
		assertEquals(2, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
	}
	
	
	@Test
	public void testDouble() {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);

		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		
		URI aliceTradeId = uri("http://example.com/trade/alice");
		URI bobTradeId = uri("http://example.com/trade/bob");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceTradeId)
				.addProperty(RDF.TYPE, Schema.TradeAction)
				.addProperty(Schema.agent, aliceId)
				.addDouble(Schema.price, 10.25)
			.endSubject()

			.beginSubject(bobTradeId)
				.addProperty(RDF.TYPE, Schema.TradeAction)
				.addProperty(Schema.agent, bobId)
				.addDouble(Schema.price, 20.99)
			.endSubject()
			;
		

		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath("TradeAction^type[price 20.99]");
		result = path.traverse(new Traverser(graph));
		assertEquals(1, result.size());
		
	}
	
	@Test
	public void testInteger() {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);

		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		
		URI aliceTradeId = uri("http://example.com/trade/alice");
		URI bobTradeId = uri("http://example.com/trade/bob");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceTradeId)
				.addProperty(RDF.TYPE, Schema.TradeAction)
				.addProperty(Schema.agent, aliceId)
				.addProperty(Schema.price, 10)
			.endSubject()

			.beginSubject(bobTradeId)
				.addProperty(RDF.TYPE, Schema.TradeAction)
				.addProperty(Schema.agent, bobId)
				.addProperty(Schema.price, 20)
			.endSubject()
			;
		

		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath("TradeAction^type[price 20]");
		result = path.traverse(new Traverser(graph));
		assertEquals(1, result.size());
		
	}

	@Test
	public void test() {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		
		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		URI cathyId = uri("http://example.com/person/cathy");
		
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addProperty(Schema.gender, Schema.Female)
				.addProperty(Schema.parent, bobId)
				.addProperty(Schema.parent, cathyId)
			.endSubject()
			.beginSubject(bobId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addProperty(Schema.gender, Schema.Male)
			.endSubject()
			.beginSubject(cathyId)
				.addProperty(RDF.TYPE, Schema.Person)
				.addProperty(Schema.gender, Schema.Female)
			.endSubject()
			;
		
		
		PathFactory factory = new PathFactory(nsManager, graph);
		
		Path path;
		Set<Value> result;

		path = factory.createPath("schema:Person^rdf:type");
		result = path.traverse(new Traverser(graph));
		assertEquals(3, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
		assertTrue(result.contains(cathyId));
		
		path = factory.createPath("schema:Person^rdf:type[schema:gender schema:Female]");
		result = path.traverse(new Traverser(graph));
		assertEquals(2, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(cathyId));
		
		Vertex alice = graph.getVertex(aliceId);
		
		path = factory.createPath("/schema:parent[schema:gender schema:Male]");
		result = path.traverse(alice);
		assertEquals(1, result.size());
		assertTrue(result.contains(bobId));
		
		path=factory.createPath("<http://schema.org/Person>^<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		result = path.traverse(new Traverser(graph));
		assertEquals(3, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
		assertTrue(result.contains(cathyId));
		
		path=factory.createPath("Person^type");
		result = path.traverse(new Traverser(graph));
		assertEquals(3, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
		assertTrue(result.contains(cathyId));
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
