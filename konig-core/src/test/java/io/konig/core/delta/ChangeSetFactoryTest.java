package io.konig.core.delta;

/*
 * #%L
 * konig-core
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

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.PROV;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;

public class ChangeSetFactoryTest {
	
	
	@Test
	public void testDoublyNestedBNodes() throws Exception {
		
		URI aliceId = uri("http://example.com/alice");
		
		Graph original = new MemoryGraph();
		
		Resource address = original.vertex().getId();
		Resource hoursAvailable = original.vertex().getId();
		
		original.edge(aliceId, Schema.address, address);
		original.edge(address, Schema.streetAddress, literal("101 Main St"));
		original.edge(address, Schema.hoursAvailable, hoursAvailable);
		original.edge(hoursAvailable, Schema.opens, time("08:00:00"));
		original.edge(hoursAvailable, Schema.closes, time("10:00:00"));
		
		Graph clone = new MemoryGraph(original);
		
		GenericBNodeKeyFactory keyFactory = new GenericBNodeKeyFactory();

		ChangeSetFactory maker = new ChangeSetFactory();
		Graph delta = maker.createChangeSet(original, clone, keyFactory);
		
		assertEquals(0,  delta.size());
		
		
	}
	
	private Value time(String value) {
		return new LiteralImpl(value, XMLSchema.TIME);
	}

	@Test
	public void testIgnoreNamespace() throws Exception {
		
		URI shapeId = uri("http://example.com/shapes/v1/Person");
		URI activity1Id = uri("http://example.com/activity/1");
		URI activity2Id = uri("http://example.com/activity/2");
		
		Graph source = new MemoryGraph();
		source.builder()
			.beginSubject(shapeId)
				.addProperty(SH.targetClass, FOAF.PERSON)
				.addProperty(PROV.wasGeneratedBy, activity1Id)
			.endSubject()
			.beginSubject(activity1Id)
				.addLiteral(PROV.endedAtTime, "2016-12-21T08:00", XMLSchema.DATETIME)
			.endSubject()
			
			;
		

		Graph target = new MemoryGraph();
		target.builder()
			.beginSubject(shapeId)
				.addProperty(SH.targetClass, Schema.Person)
				.addProperty(PROV.wasGeneratedBy, activity2Id)
			.endSubject()
			.beginSubject(activity2Id)
				.addLiteral(PROV.endedAtTime, "2016-12-22T09:00", XMLSchema.DATETIME)
			.endSubject();
		
		SimpleKeyFactory keyFactory = new SimpleKeyFactory(SH.property, SH.predicate);

		Set<String> ignoreNamespace = new HashSet<>();
		ignoreNamespace.add("http://example.com/activity/");
		
		ChangeSetFactory maker = new ChangeSetFactory();
		maker.setIgnoreNamespace(ignoreNamespace);
		Graph delta = maker.createChangeSet(source, target, keyFactory);
		
		
		Vertex shape = delta.getVertex(shapeId);
		assertTrue(shape != null);
		
		assertChange(shape, SH.targetClass, FOAF.PERSON, Konig.Falsehood);
		assertChange(shape, SH.targetClass, Schema.Person, Konig.Dictum);
		assertEquals(2, delta.size());
		
		
//		NamespaceManager nsManager = new MemoryNamespaceManager();
//		nsManager.add("schema", Schema.NAMESPACE);
//		nsManager.add("sh", SH.NAMESPACE);
//		nsManager.add("foaf", FOAF.NAMESPACE);
//		nsManager.add("prov", PROV.NAMESPACE);
//		nsManager.add("activity", "http://example.com/activity/");
//		
//		
//		PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
//	
//		reporter.write(delta, System.out);
		
	}
	
	private void assertChange(Vertex shape, URI predicate, Value object, URI disposition) {
		
		Set<Edge> set = shape.outProperty(predicate);
		for (Edge edge : set) {
			
			Value value = edge.getObject();
			if (value.equals(object)) {
				assertEquals(disposition, edge.getAnnotation(RDF.TYPE));
				return;
			}
		}
		
	}

	@Test
	public void testNoChangeBNode() throws Exception {
		URI shapeId = uri("http://example.com/shapes/v1/Person");
		
		Graph source = new MemoryGraph();
		
		source.builder()
			.beginSubject(shapeId)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.email)
					.addInt(SH.minCount, 0)
					.addInt(SH.maxCount, 1)
				.endSubject()
			.endSubject();
		
		Graph target = new MemoryGraph();
		target.builder()
			.beginSubject(shapeId)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.email)
					.addInt(SH.minCount, 0)
					.addInt(SH.maxCount, 1)
				.endSubject()
			.endSubject();

		
		SimpleKeyFactory keyFactory = new SimpleKeyFactory(SH.property, SH.predicate);
		
		ChangeSetFactory maker = new ChangeSetFactory();
		Graph delta = maker.createChangeSet(source, target, keyFactory);
		
		assertEquals(0, delta.size());
	}
	
	
	@Test
	public void testPropertyConstraint() throws Exception {
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		URI shapeId = uri("http://example.com/shapes/v1/Person");
		
		Graph source = new MemoryGraph();
		
		source.builder()
			.beginSubject(shapeId)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.email)
					.addInt(SH.minCount, 0)
					.addInt(SH.maxCount, 1)
				.endSubject()
			.endSubject();
		
		Graph target = new MemoryGraph();
		target.builder()
			.beginSubject(shapeId)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.email)
					.addInt(SH.minCount, 0)
				.endSubject()
			.endSubject();

		
		SimpleKeyFactory keyFactory = new SimpleKeyFactory(SH.property, SH.predicate);
		
		ChangeSetFactory maker = new ChangeSetFactory();
		Graph delta = maker.createChangeSet(source, target, keyFactory);
		
		// TODO: add tests
		
//		PlainTextChangeSetReportWriter writer = new PlainTextChangeSetReportWriter(nsManager);
//		writer.write(delta, System.out);
	}
	
	@Test
	public void testList() throws Exception {
		
		URI itemList = uri("http://schema.example.com/itemList");
		URI apple = uri("http://dbpedia.org/resource/Apple");
		URI orange = uri("http://dbpedia.org/resource/Orange");
		URI banana = uri("http://dbpedia.org/resource/Banana");
		URI grape = uri("http://dbpedia.org/resource/Grape");
		URI plum = uri("http://dbpedia.org/resource/Plum");
		URI pear = uri("http://dbpedia.org/resource/Pear");
		
		URI cart = uri("http://data.example.com/cart/1");
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("dbpedia", "http://dbpedia.org/resource/" );
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		
		Graph source = new MemoryGraph();
		source.builder()
			.beginSubject(cart)
				.addList(itemList, apple, orange, banana, grape)
			.endSubject();
		
		Graph target = new MemoryGraph();
		target.builder()
			.beginSubject(cart)
				.addList(itemList, apple, orange, grape, plum, pear)
			.endSubject();

		GenericBNodeKeyFactory keyFactory = new GenericBNodeKeyFactory();
		ChangeSetFactory maker = new ChangeSetFactory();
		Graph delta = maker.createChangeSet(source, target, keyFactory);
		
		// TODO: Add assertions

//		PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
//		reporter.write(delta, System.out);
		
	}

	@Test
	public void test() throws Exception {
		
		URI alice = uri("http://example.com/alice");
		
		Graph source = new MemoryGraph();
		source.builder()
			.beginSubject(alice)
				.addLiteral(Schema.givenName, "Alice")
				.addLiteral(Schema.familyName, "Jones")
				.addLiteral(Schema.email, "alice@example.com")
				.beginBNode(Schema.contactPoint) 
					.addLiteral(Schema.contactType, "Work")
					.addLiteral(Schema.telephone, "555-123-4567")
				.endSubject()
			.endSubject();
		
		Graph target = new MemoryGraph();
		target.builder()
			.beginSubject(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addProperty(Schema.gender, Schema.Female)
			.beginBNode(Schema.contactPoint) 
				.addLiteral(Schema.contactType, "Work")
				.addLiteral(Schema.telephone, "555-987-6543")
			.endSubject()
			.addLiteral(Schema.familyName, "Smith")
		.endSubject();
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		ChangeSetFactory maker = new ChangeSetFactory();
		BNodeKeyFactory keyExtractor = new SimpleKeyFactory(Schema.contactPoint, Schema.contactType);
		Graph delta = maker.createChangeSet(source, target, keyExtractor);
		
		Vertex aliceNode = delta.getVertex(alice);
		assertTrue(aliceNode != null);
		
		assertUndefined(aliceNode, Schema.givenName);
		assertLiteral(aliceNode, Schema.familyName, "Jones", Konig.Falsehood);
		assertLiteral(aliceNode, Schema.familyName, "Smith", Konig.Dictum);
		
//		PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
//		reporter.write(delta, System.out);
		
		Set<Edge> contactPoint = aliceNode.outProperty(Schema.contactPoint);
		assertEquals(1, contactPoint.size());
		
		Edge contactPointEdge = contactPoint.iterator().next();
		assertEquals(Konig.KeyValue, contactPointEdge.getAnnotation(RDF.TYPE));
		
		Vertex contactPointNode = aliceNode.asTraversal().out(Schema.contactPoint).firstVertex();
		assertLiteral(contactPointNode, Schema.contactType, "Work", Konig.KeyValue);
		assertLiteral(contactPointNode, Schema.telephone, "555-123-4567", Konig.Falsehood);
		assertLiteral(contactPointNode, Schema.telephone, "555-987-6543", Konig.Dictum);
		assertValue(contactPointNode, Schema.gender, Schema.Female, Konig.Dictum);
		
	}

	private void assertValue(Vertex subject, URI predicate, URI object, URI function) {

		
		Set<Edge> set = subject.outProperty(predicate);
		for (Edge edge : set) {
			Value v = edge.getObject();
			if (v.equals(object)) {
				assertEquals(function, edge.getAnnotation(RDF.TYPE));
				return;
			}
		}
	}

	private void assertUndefined(Vertex subject, URI predicate) {
		Set<Edge> set = subject.outProperty(predicate);
		assertTrue(set.isEmpty());
		
	}

	private void assertLiteral(Vertex subject, URI predicate, String value, URI function) {
		
		Literal object = literal(value);
		
		Set<Edge> set = subject.outProperty(predicate);
		for (Edge edge : set) {
			Value v = edge.getObject();
			if (v.equals(object)) {
				assertEquals(function, edge.getAnnotation(RDF.TYPE));
				return;
			}
		}
	}

	private Literal literal(String value) {
		
		return new LiteralImpl(value);
	}

	private URI uri(String text) {
		return new URIImpl(text);
	}
	
	

}
