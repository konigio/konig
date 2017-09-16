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

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class BasicPojoBuilderTest {

	private MemoryGraph graph = new MemoryGraph();
	private SimplePojoFactory factory = new SimplePojoFactory();
	
	@Test
	public void testPropertyConstraint() {
		
		URI shapeId = uri("http://example.com/PersonShape");
		Resource givenNameProperty = graph.vertex().getId();
		graph.edge(shapeId, SH.property, givenNameProperty);
		graph.edge(givenNameProperty, SH.path, Schema.givenName);
		
		Vertex v = graph.getVertex(shapeId);
		Shape shape = factory.create(v, Shape.class);
		
		List<PropertyConstraint> list = shape.getProperty();
		
		assertEquals(1, list.size());
		PropertyConstraint p = list.get(0);
		
		assertEquals(Schema.givenName, p.getPredicate());
	}
	
	@Ignore
	public void testProduct() {
		URI iphone = uri("http://examplelcom/iphone");
		Value name = literal("iPhone");
		URI apple = uri("http://example.com/Apple");
		graph.edge(iphone, Schema.name, name);
		graph.edge(iphone, Schema.manufacturer, apple);
		
		Vertex v = graph.getVertex(iphone);
		TestProduct product = factory.create(v, TestProduct.class);
		
		assertEquals(name, product.getName());
		assertEquals(apple, product.getManufacturer());
		
	}

	@Ignore
	public void testPerson() {
		
		
		URI alice = uri("http://example.com/alice");
		Resource address = graph.vertex().getId();
		graph.edge(alice, RDF.TYPE, Schema.Person);
		graph.edge(alice, Schema.name, literal("Alice"));
		graph.edge(alice, Schema.gender, Schema.Female);
		graph.edge(alice, Schema.address, address);
		graph.edge(address, Schema.addressLocality, literal("100 Main Street"));
		Vertex v = graph.getVertex(alice);
		
		TestPerson person = factory.create(v, TestPerson.class);
		TestAddress javaAddress = person.getAddress();
		
		assertEquals("Alice", person.getName());
		assertEquals(TestGender.Female, person.getGender());
		assertTrue(javaAddress != null);
		
		assertEquals("100 Main Street", javaAddress.getAddressLocality());
		
		
	}

	private Literal literal(String value) {
		return new LiteralImpl(value);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
