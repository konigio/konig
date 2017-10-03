package io.konig.core.pojo;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

import io.konig.annotation.RdfList;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.TemporalUnit;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.Shape;

public class PojoFactoryTest {
	
	private SimplePojoFactory factory = new SimplePojoFactory();
	
	@Test
	public void testPropertyPath() throws Exception {
		
		URI rootId = uri("http://example.com/root");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(rootId)
				.addList(SH.path, Schema.address, Schema.addressCountry)
			.endSubject();

		Vertex v = graph.getVertex(rootId);
		
		TestPropertyShape pojo = factory.create(v, TestPropertyShape.class);
		
		TestPropertyPath path = pojo.getPath();
		assertTrue(path instanceof TestSequencePath);
		
		TestSequencePath sequencePath = (TestSequencePath) path;
		assertEquals(2, sequencePath.size());
		
	
		
	}
	
	@Test
	public void testRdfListOwner() throws Exception {
		URI ownerId = uri("http://example.com/owner");
		URI pathPredicate = uri("http://example.com/schema/path");
		URI pathElement = uri("http://example.com/element");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(ownerId)
				.addList(pathPredicate, pathElement)
			.endSubject();
		
		Vertex v = graph.getVertex(ownerId);

		
		PathListOwner owner = factory.create(v, PathListOwner.class);
		
		PathList pathList = owner.getPath();
		assertTrue(pathList != null);
		assertEquals(1, pathList.getList().size());
	}
	
	
	@Test
	public void testStructuredListOwner() throws Exception {
		URI ownerId = uri("http://example.com/owner");
		URI predicate = uri("http://example.com/schema/list");
		URI aliceId = uri("http://example.com/alice");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(ownerId)
				.addList(predicate, aliceId)
			.endSubject()
			.beginSubject(aliceId)
				.addLiteral(Schema.name, "Alice")
			.endSubject()
			;
		
		Vertex v = graph.getVertex(ownerId);

		
		StructuredListOwner owner = factory.create(v, StructuredListOwner.class);
		
		StructuredList pathList = owner.getList();
		assertTrue(pathList != null);
		assertEquals(1, pathList.getList().size());
		NamedPerson person = pathList.getList().get(0);
		
		
		assertEquals("Alice", person.getName());
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
		
		StructuredWishlist pojo = factory.create(v, StructuredWishlist.class);
		TestPerson alice = pojo.getWishlistOwner();
		assertEquals(alice.getName(), "Alice");
		
		
		List<Product> actual = pojo.getWishlist();
		assertEquals(3, actual.size());
		assertEquals(galaxy7, actual.get(0).getId());
		assertEquals("Galaxy 7", actual.get(0).getName());
		assertEquals(iphone7, actual.get(1).getId());
		assertEquals(motoG4, actual.get(2).getId());
	}
	
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
		
		Shape shape = factory.create(v, Shape.class);
		
		IriTemplate template = shape.getIriTemplate();
		assertTrue(template != null);
		
		assertEquals("<http://example.com/person/{person_id}>", template.toString());
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
		

		
		Shape shape = factory.create(v, Shape.class);
		
		OrConstraint orList = shape.getOr();
		
		assertTrue(orList != null);
		
		List<Shape> list = orList.getShapes();
		
		assertTrue(list != null);
		assertEquals(2, list.size());
		
		assertEquals(personShapeId, list.get(0).getId());
		assertEquals(orgShapeId, list.get(1).getId());
		
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
		
		PersonWishlist pojo = factory.create(v, PersonWishlist.class);
		assertEquals(pojo.getName(), "Alice");
		
		List<URI> actual = pojo.getWishlist();
		assertEquals(galaxy7, actual.get(0));
		assertEquals(iphone7, actual.get(1));
		assertEquals(motoG4, actual.get(2));
	}

	
	@Test
	public void testList() {
		MemoryGraph graph = new MemoryGraph();
		URI aliceId = uri("http://example.com/alice");
		
		graph.builder().beginSubject(aliceId)
			.addLiteral(Schema.name, "Alice")
			.addLiteral(Schema.name, "Enigma");
		
		Vertex v = graph.vertex(aliceId);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		Person person = factory.create(v, Person.class);
		
		List<String> list = person.getName();
		assertTrue(list != null);
		assertTrue(list.contains("Alice"));
		assertTrue(list.contains("Enigma"));
		
	}
	
	@Test
	public void testAppendToObject() throws Exception {
		

		URI predicate = uri("http://example.com/schema/productList");
		URI iphone7 = uri("http://example.com/product/iphone7");
		URI galaxy7 = uri("http://example.com/product/galaxy7");
		URI owner = uri("http://example.com/owner");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(owner)
				.addList(predicate, iphone7, galaxy7)
			.endSubject();
		
		Vertex v = graph.getVertex(owner);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		AppendToObject pojo = factory.create(v, AppendToObject.class);
		
		List<URI> productList = pojo.getProductList();
		assertEquals(2, productList.size());
		assertEquals(iphone7, productList.get(0));
		assertEquals(galaxy7, productList.get(1));
	}
	
	@Test
	public void testAppenderObject() throws Exception {
		

		URI predicate = uri("http://example.com/schema/product");
		URI iphone7 = uri("http://example.com/product/iphone7");
		URI galaxy7 = uri("http://example.com/product/galaxy7");
		URI owner = uri("http://example.com/owner");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(owner)
				.addProperty(predicate, iphone7)
				.addProperty(predicate, galaxy7)
			.endSubject();
		
		Vertex v = graph.getVertex(owner);
		
		AppenderObject pojo = factory.create(v, AppenderObject.class);
		
		List<URI> productList = pojo.getProductList();
		assertEquals(2, productList.size());
		assertEquals(iphone7, productList.get(0));
		assertEquals(galaxy7, productList.get(1));
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
		
		PojoContext context = factory.getContext();
		context.mapClass(Schema.Person, TestPerson.class);
		
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
		
		PojoContext context = factory.getContext();
		context.mapClass(Schema.Person, TestPerson.class);
		
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
		
		TimeValue pojo = factory.create(v, TimeValue.class);
		
		assertEquals(2.5f, pojo.getValue(), 0.0001);
		assertEquals(TemporalUnit.HOUR, pojo.getTimeUnit());
		 
	}
	
	@Test
	public void testDeepNesting() throws Exception {
		
		URI rootId = uri("http://example.com/root");
		URI alpha = uri("http://example.com/schema/alpha");
		URI beta = uri("http://example.com/schema/beta");
		URI gamma = uri("http://example.com/schema/gamma");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(rootId)
				.addLiteral(Schema.name, "level1")
				.beginBNode(alpha)
					.addLiteral(Schema.name, "level2")
					.beginBNode(beta)
						.addLiteral(Schema.name, "level3")
						.beginBNode(gamma)
							.addLiteral(Schema.name, "level4")
						.endSubject()
					.endSubject()
				.endSubject()
			.endSubject();

		Vertex v = graph.getVertex(rootId);
		
		Level1 pojo = factory.create(v, Level1.class);
		
		assertEquals("level1", pojo.getName());
		
		Level2 level2 = pojo.getAlpha();
		assertEquals("level2", level2.getName());
		
		List<Level3> level3List = level2.getBeta();
		assertEquals(1, level3List.size());
		Level3 level3 = level3List.get(0);
		assertEquals("level3", level3.getName());
		
		Level4 level4 = level3.getGamma();
		assertEquals("level4", level4.getName());
		
	}
	
	@Test
	public void testStringLiteral() {
		URI aliceId = uri("http://example.com/alice");
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceId)
				.addLiteral(Schema.name, "Alice")
			.endSubject();
		
		Vertex v = graph.getVertex(aliceId);
		
		TestPerson person = factory.create(v, TestPerson.class);
		
		assertEquals("Alice", person.getName());
	}
	

	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	
	static class Thing {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}

	public static class Level1 extends Thing {
		private Level2 alpha;

		public Level2 getAlpha() {
			return alpha;
		}

		public void setAlpha(Level2 alpha) {
			this.alpha = alpha;
		}
		
		
	}
	
	public static class Level2 extends Thing {
		private List<Level3> beta;

		public List<Level3> getBeta() {
			return beta;
		}

		public void setBeta(List<Level3> beta) {
			this.beta = beta;
		}

		
		
	}
	
	public static class Level3 extends Thing {
		private Level4 gamma;

		public Level4 getGamma() {
			return gamma;
		}

		public void setGamma(Level4 gamma) {
			this.gamma = gamma;
		}
		
	}
	
	public static class Level4 extends Thing {
		
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
	static public class AppenderObject {

		private List<URI> productList = new ArrayList<>();
		
		public void addProduct(URI product) {
			productList.add(product);
		}

		public List<URI> getProductList() {
			return productList;
		}
	}
	
	static public class AppendToObject {
		private List<URI> productList = new ArrayList<>();
		
		public void appendToProductList(URI email) {
			productList.add(email);
		}

		public List<URI> getProductList() {
			return productList;
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
	}

	public static class StructuredListOwner {
		private StructuredList list;

		public StructuredList getList() {
			return list;
		}

		public void setList(StructuredList list) {
			this.list = list;
		}
	}

	@RdfList
	public static class StructuredList {
		private List<NamedPerson> list = new ArrayList<>();
		
		public void add(NamedPerson person) {
			list.add(person);
		}
		
		public List<NamedPerson> getList() {
			return list;
		}
		
	}

	public static class NamedPerson {
		private URI id;
		private String name;
		public URI getId() {
			return id;
		}
		public void setId(URI id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
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
		
//		public void addWishlist(Product product) {
//			if (wishlist == null) {
//				wishlist = new ArrayList<>();
//			}
//			wishlist.add(product);
//		}
		public Resource getId() {
			return id;
		}
		public void setId(Resource id) {
			this.id = id;
		}
		
	}

	public static class PathListOwner {
		private PathList pathList;

		public PathList getPath() {
			return pathList;
		}

		public void setPath(PathList pathList) {
			this.pathList = pathList;
		}
		
	}
	
	@RdfList
	public static class PathList {
		private List<URI> list = new ArrayList<>();
		
		public void add(URI uri) {
			list.add(uri);
		}
		
		public List<URI> getList() {
			return list;
		}
	}
}
