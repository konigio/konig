package io.konig.core.pojo;

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

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.TemporalUnit;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.Shape;

public class SimplePojoFactoryTest {
	
	@Test
	public void testStringLiteralConstructor() throws Exception {
		
		URI personShapeId = uri("http://example.com/shapes/1/schema/PersonShape");
		
		MemoryGraph graph = new MemoryGraph();
		
		graph.builder()
		
			
			.beginSubject(personShapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addLiteral(Konig.iriTemplate, "http://example.com/person/{person_id}" )
			.endSubject()
			;
		
		Vertex v = graph.getVertex(personShapeId);
		

		SimplePojoFactory factory = new SimplePojoFactory();
		
		Shape shape = factory.create(v, Shape.class);
		
		IriTemplate template = shape.getIriTemplate();
		assertTrue(template != null);
		
		assertEquals("http://example.com/person/{person_id}", template.toString());
	}
	
	@Test
	public void testRdfListAnnotation() throws Exception {
		
		
		URI partyShapeId = uri("http://example.com/shapes/1/schema/PartyShape");
		URI personShapeId = uri("http://example.com/shapes/1/schema/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/1/schema/OrganizationShape");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
		
			.beginSubject(partyShapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addList(SH.or, personShapeId, orgShapeId)
			.endSubject()
			
			.beginSubject(personShapeId)
				.addProperty(RDF.TYPE, SH.Shape)
			.endSubject()

			.beginSubject(orgShapeId)
				.addProperty(RDF.TYPE, SH.Shape)
			.endSubject();
		
		Vertex v = graph.getVertex(partyShapeId);
		

		SimplePojoFactory factory = new SimplePojoFactory();
		
		Shape shape = factory.create(v, Shape.class);
		
		OrConstraint orList = shape.getOr();
		
		assertTrue(orList != null);
		
		List<Shape> list = orList.getShapes();
		
		assertTrue(list != null);
		assertEquals(2, list.size());
		
		assertEquals(list.get(0).getId(), personShapeId);
		assertEquals(list.get(1).getId(), orgShapeId);
		
	}
	
	@Test
	public void testStructuredList() throws Exception {
		
		URI wishlist = uri("http://example.com/vocab/wishlist");
		URI wishlistOwner = uri("http://example.com/vocab/wishlistOwner");
		URI wishlistId = uri("http://example.com/list/1");
		URI aliceId = uri("http://example.com/person/alice");
		URI galaxy7 = uri("http://example.com/product/galaxy7");
		URI iphone7 = uri("http://example.com/product/iphone7");
		URI motoG4 = uri("http://example.com/product/motoG4");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(galaxy7)
				.addLiteral(Schema.name, "Galaxy 7")
			.endSubject()
			.beginSubject(iphone7)
				.addLiteral(Schema.name, "iPhone 7")
			.endSubject()
			.beginSubject(motoG4)
				.addLiteral(Schema.name, "Moto G4")
			.endSubject()
			.beginSubject(wishlistId)
				.addProperty(wishlistOwner, aliceId)
				.beginSubject(aliceId)
					.addLiteral(Schema.name, "Alice")
				.endSubject()
				.addList(wishlist, galaxy7, iphone7, motoG4)
			.endSubject();
	
		Vertex v = graph.getVertex(wishlistId);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		StructuredWishlist pojo = factory.create(v, StructuredWishlist.class);
		TestPerson alice = pojo.getWishlistOwner();
		assertEquals(alice.getName(), "Alice");
		
		
		List<Product> actual = pojo.getWishlist();
		assertEquals(galaxy7, actual.get(0).getId());
		assertEquals("Galaxy 7", actual.get(0).getName());
		assertEquals(iphone7, actual.get(1).getId());
		assertEquals(motoG4, actual.get(2).getId());
	}
	
	@Test
	public void testRdfList() throws Exception {

		URI wishlist = uri("http://example.com/vocab/wishlist");
		URI aliceId = uri("http://example.com/person/alice");
		URI galaxy7 = uri("http://example.com/product/galaxy7");
		URI iphone7 = uri("http://example.com/product/iphone7");
		URI motoG4 = uri("http://example.com/product/motoG4");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceId)
				.addLiteral(Schema.name, "Alice")
				.addList(wishlist, galaxy7, iphone7, motoG4)
			.endSubject();
	
		Vertex v = graph.getVertex(aliceId);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		PersonWishlist pojo = factory.create(v, PersonWishlist.class);
		assertEquals(pojo.getName(), "Alice");
		
		List<URI> actual = pojo.getWishlist();
		assertEquals(galaxy7, actual.get(0));
		assertEquals(iphone7, actual.get(1));
		assertEquals(motoG4, actual.get(2));
	}
	
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
		
		SimplePojoFactory factory = new SimplePojoFactory(context);
		factory.createAll(graph);
		
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
		
		SimplePojoFactory factory = new SimplePojoFactory(context);
		factory.createAll(graph);
		
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
	
	public static class Product {
		private Resource id;
		private String name;
		public Resource getId() {
			return id;
		}
		public void setId(Resource id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		
	}
	
	public static class StructuredWishlist {
		private Resource id;
		private TestPerson wishlistOwner;
		private List<Product> wishlist;
		
		
		
		public TestPerson getWishlistOwner() {
			return wishlistOwner;
		}
		public void setWishlistOwner(TestPerson wishlistOwner) {
			this.wishlistOwner = wishlistOwner;
		}
		public List<Product> getWishlist() {
			return wishlist;
		}
		public void setWishlist(List<Product> wishlist) {
			this.wishlist = wishlist;
		}
		
		public void addWishlist(Product product) {
			if (wishlist == null) {
				wishlist = new ArrayList<>();
			}
			wishlist.add(product);
		}
		public Resource getId() {
			return id;
		}
		public void setId(Resource id) {
			this.id = id;
		}
		
	}
	
	public static class PersonWishlist {
		private Resource id;
		private String name;
		private List<URI> wishlist;
		
		public Resource getId() {
			return id;
		}
		public void setId(Resource id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<URI> getWishlist() {
			return wishlist;
		}
		public void setWishlist(List<URI> wishlist) {
			this.wishlist = wishlist;
		}
		
		public void addWishlist(URI value) {
			if (wishlist == null) {
				wishlist = new ArrayList<>();
			}
			wishlist.add(value);
		}
	}

}
