package io.konig.pojo.io;

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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.pojo.PojoContext;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.TemporalUnit;

public class SimplePojoFactoryTest {
	
	@Test
	public void testAll() {
		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder().beginSubject(aliceId)
			.addProperty(RDF.TYPE, Schema.Person)
			.addLiteral(Schema.name, "Alice")
		.endSubject()
		.beginSubject(bobId)
			.addProperty(RDF.TYPE, Schema.Person)
			.addLiteral(Schema.name, "Bob")
		.endSubject();
		
		PojoContext context = new PojoContext();
		context.mapClass(Schema.Person, TestPerson.class);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		factory.createAll(graph, context);
		
		TestPerson alice = context.getIndividual(aliceId, TestPerson.class);
		assertTrue(alice != null);
		assertEquals("Alice", alice.getName());
		
		TestPerson bob = context.getIndividual(bobId, TestPerson.class);
		assertTrue(bob != null);
		assertEquals("Bob", bob.getName());
		
	}

	@Test
	public void test() {
		
		URI aliceId = uri("http://example.com/person/alice");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder().beginSubject(aliceId)
			.addProperty(RDF.TYPE, Schema.Person)
			.addLiteral(Schema.name, "Alice")
			.beginBNode(Schema.address)
				.addLiteral(Schema.streetAddress, "101 Main Street")
				.addLiteral(Schema.addressLocality, "Gaithersburg")
				.addLiteral(Schema.addressRegion, "MD")
			.endSubject()
		.endSubject();
		
		PojoContext context = new PojoContext();
		context.mapClass(Schema.Person, TestPerson.class);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		factory.createAll(graph, context);
		
		TestPerson alice = context.getIndividual(aliceId, TestPerson.class);
		assertTrue(alice != null);
		assertEquals("Alice", alice.getName());
		
		TestAddress address = alice.getAddress();
		assertTrue(address!=null);
		assertEquals("101 Main Street", address.getStreetAddress());
		assertEquals("Gaithersburg", address.getAddressLocality());
		assertEquals("MD", address.getAddressRegion());
		
	}

	@Test
	public void testList() {
		MemoryGraph graph = new MemoryGraph();
		URI aliceId = uri("http://example.com/alice");
		
		graph.builder().beginSubject(aliceId)
			.addLiteral(Schema.name, "Alice")
			.addLiteral(Schema.name, "Babe");
		
		Vertex v = graph.vertex(aliceId);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		Person person = factory.create(v, Person.class);
		
		List<String> list = person.getName();
		assertTrue(list != null);
		
	}

	@Test
	public void testEnumValue() {
		
		URI TimeValueClass = uri("http://schema.example.com/TimeValue");
		URI valuePredicate = uri("http://schema.example.com/value");
		URI unitPredicate = uri("http://schema.example.com/timeUnit");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder().beginSubject()
			.addProperty(RDF.TYPE, TimeValueClass)
			.addFloat(valuePredicate, 2.5f)
			.addProperty(unitPredicate, TemporalUnit.HOUR.getURI());
		
		Vertex v = graph.v(TimeValueClass).in(RDF.TYPE).firstVertex();

		SimplePojoFactory factory = new SimplePojoFactory();
		
		TimeValue pojo = factory.create(v, TimeValue.class);
		
		assertEquals(2.5f, pojo.getValue(), 0.0001);
		assertEquals(TemporalUnit.HOUR, pojo.getTimeUnit());
		 
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	public static class Person {
		private URI id;
		private List<String> name;
		
		public URI getId() {
			return id;
		}
		public void setId(URI id) {
			this.id = id;
		}
		public List<String> getName() {
			return name;
		}
		public void setName(List<String> name) {
			this.name = name;
		}
		
		public void addName(String name) {
			if (this.name == null) {
				this.name = new ArrayList<>();
			}
			this.name.add(name);
		}
		
		
	}
	
	public static class TimeValue {
		private float value;
		private TemporalUnit timeUnit;
		public float getValue() {
			return value;
		}
		public void setValue(float value) {
			this.value = value;
		}
		public TemporalUnit getTimeUnit() {
			return timeUnit;
		}
		public void setTimeUnit(TemporalUnit timeUnit) {
			this.timeUnit = timeUnit;
		}
	}

}
