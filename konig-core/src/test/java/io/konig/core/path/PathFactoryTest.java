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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.PathFactory;
import io.konig.core.Traverser;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class PathFactoryTest {

	private PathFactory factory = new PathFactory();
	
	private String context(NamespaceManager nsManager) {
		StringBuilder builder = new StringBuilder();
		builder.append("@context {");
		String comma = "\n  ";
		for (Namespace ns : nsManager.listNamespaces()) {
			builder.append(comma);
			builder.append('"');
			builder.append(ns.getPrefix());
			builder.append("\" : \"");
			builder.append(ns.getName());
			builder.append('"');
			comma = ",\n  ";
		}
		builder.append("\n}\n");
		return builder.toString();
	}
	
	private String loadText(String path) throws IOException {
		return new String(Files.readAllBytes(Paths.get(path)));
	}
	
	@Test
	public void testVariable() throws Exception {
		String text = loadText("src/test/resources/PathFactoryTest/variable.txt");
		
		Path path = factory.createPath(text);
		List<Step> stepList = path.asList();
		assertEquals(2, stepList.size());
		OutStep var = (OutStep) stepList.get(0);
		assertEquals("?x", var.getPredicate().getLocalName());
		OutStep out = (OutStep) stepList.get(1);
		assertEquals(Schema.name, out.getPredicate());
	}
	
	
	@Ignore
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
		
		
		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath(context(nsManager)+"schema:CreativeWork^rdf:type[schema:isFamilyFriendly true]");
		result = path.traverse(new Traverser(graph));
		
		assertEquals(2, result.size());
		assertTrue(result.contains(one));
		assertTrue(result.contains(three));
	}
	
	@Ignore
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
		
		
		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath(context(nsManager)+"schema:Person^rdf:type[schema:birthDate \"1996-11-13\"^^xsd:date]");
		result = path.traverse(new Traverser(graph));
		assertEquals(2, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
	}
	
	
	@Ignore
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
		
		
		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath(context(nsManager) + "schema:Thing^rdf:type[schema:name \"fruit\"@fr]");
		result = path.traverse(new Traverser(graph));
		assertEquals(1, result.size());
		assertTrue(result.contains(two));
	}
	
	@Ignore
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
		
		
		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath(context(nsManager) + "schema:Person^rdf:type[schema:familyName \"Smith\"]");
		result = path.traverse(new Traverser(graph));
		
		assertEquals(2, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
	}
	
	
	@Ignore
	public void testDouble() {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		nsManager.add("TradeAction", Schema.TradeAction.stringValue());
		nsManager.add("type", RDF.TYPE.stringValue());
		nsManager.add("price", Schema.price.stringValue());

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
		

		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath(context(nsManager)+"TradeAction^type[price 20.99]");
		result = path.traverse(new Traverser(graph));
		assertEquals(1, result.size());
		
	}
	
	@Ignore
	public void testInteger() {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		nsManager.add("TradeAction", Schema.TradeAction.stringValue());
		nsManager.add("type", RDF.TYPE.stringValue());
		nsManager.add("price", Schema.price.stringValue());

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
		

		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		path = factory.createPath(context(nsManager)+"TradeAction^type[price 20]");
		result = path.traverse(new Traverser(graph));
		assertEquals(1, result.size());
		
	}

	@Ignore
	public void test() {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		nsManager.add("Person", Schema.Person.stringValue());
		nsManager.add("type", RDF.TYPE.stringValue());
		
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
		
		
		PathFactory factory = new PathFactory();
		
		Path path;
		Set<Value> result;
		
		String ctx = context(nsManager);

		path = factory.createPath(ctx+"schema:Person^rdf:type");
		result = path.traverse(new Traverser(graph));
		assertEquals(3, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
		assertTrue(result.contains(cathyId));
		
		path = factory.createPath(ctx+"schema:Person^rdf:type[schema:gender schema:Female]");
		result = path.traverse(new Traverser(graph));
		assertEquals(2, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(cathyId));
		
		Vertex alice = graph.getVertex(aliceId);
		
		path = factory.createPath(ctx+"/schema:parent[schema:gender schema:Male]");
		result = path.traverse(alice);
		assertEquals(1, result.size());
		assertTrue(result.contains(bobId));
		
		path=factory.createPath(ctx+"<http://schema.org/Person>^<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		result = path.traverse(new Traverser(graph));
		assertEquals(3, result.size());
		assertTrue(result.contains(aliceId));
		assertTrue(result.contains(bobId));
		assertTrue(result.contains(cathyId));
		
		path=factory.createPath(ctx+"Person^type");
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
