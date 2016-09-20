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


import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.CS;
import io.konig.core.vocab.Schema;

public class ChangeSetMakerTest {
	
	@Ignore
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

		ChangeSetMaker maker = new ChangeSetMaker();
		Graph delta = maker.computeDelta(source, target, null);
		
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
		
		ChangeSetMaker maker = new ChangeSetMaker();
		KeyExtractor keyExtractor = new SimpleKeyExtractor(Schema.contactPoint, Schema.contactType);
		Graph delta = maker.computeDelta(source, target, keyExtractor);
		
		Vertex aliceNode = delta.getVertex(alice);
		assertTrue(aliceNode != null);
		
		assertUndefined(aliceNode, Schema.givenName);
		assertLiteral(aliceNode, Schema.familyName, "Jones", CS.Remove);
		assertLiteral(aliceNode, Schema.familyName, "Smith", CS.Add);
		
		PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
		reporter.write(delta, System.out);
		
		Set<Edge> contactPoint = aliceNode.outProperty(Schema.contactPoint);
		assertEquals(1, contactPoint.size());
		
		Edge contactPointEdge = contactPoint.iterator().next();
		assertEquals(CS.Key, contactPointEdge.getProperty(CS.function));
		
		Vertex contactPointNode = aliceNode.asTraversal().out(Schema.contactPoint).firstVertex();
		assertLiteral(contactPointNode, Schema.contactType, "Work", CS.Key);
		assertLiteral(contactPointNode, Schema.telephone, "555-123-4567", CS.Remove);
		assertLiteral(contactPointNode, Schema.telephone, "555-987-6543", CS.Add);
		assertValue(contactPointNode, Schema.gender, Schema.Female, CS.Add);
		
	}

	private void assertValue(Vertex subject, URI predicate, URI object, URI function) {

		
		Set<Edge> set = subject.outProperty(predicate);
		for (Edge edge : set) {
			Value v = edge.getObject();
			if (v.equals(object)) {
				assertEquals(function, edge.getProperty(CS.function));
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
				assertEquals(function, edge.getProperty(CS.function));
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
