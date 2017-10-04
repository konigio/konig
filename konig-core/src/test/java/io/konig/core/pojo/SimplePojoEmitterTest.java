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

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.path.PathImpl;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SimplePojoEmitterTest {
	
	
	@Test
	public void testLong() throws Exception {
		URI id = uri("http://example.com/question");
		Question question = new Question(id, 1L);
		Graph graph = new MemoryGraph();
		SimplePojoEmitter emitter = new SimplePojoEmitter();

		EmitContext context = new EmitContext(graph);
		
		emitter.emit(context, question, graph);
		
		Vertex v = graph.getVertex(id);
		assertTrue(v != null);
		
		URI predicate = uri("http://schema.org/answerCount");
		Value value = v.getValue(predicate);
		assertTrue(value instanceof Literal);
		Literal literal = (Literal) value;
		assertEquals(1L, literal.longValue());
		
	}
	
	@Test
	public void testToValueMethodWithNamespaceManager() throws Exception {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("alias", "http://example.com/alias/");
		URI propertyId = uri("http://example.com/property/1");
		URI first_name = uri("http://example.com/alias/first_name");
		PropertyConstraint p = new PropertyConstraint(first_name);
		
		
		
		Path path = new PathImpl();
		path.out(first_name);
		
		p.setId(propertyId);
		p.setEquivalentPath(path);
		
		Graph graph = new MemoryGraph();
		graph.setNamespaceManager(nsManager);

		SimplePojoEmitter emitter = new SimplePojoEmitter();
		EmitContext context = new EmitContext(graph);
		
		emitter.emit(context, p, graph);
		
		Vertex v = graph.getVertex(propertyId);
		assertTrue(v != null);
		
		Value pathValue = v.getValue(Konig.equivalentPath);
		assertTrue(pathValue != null);
		
		assertEquals("/<http://example.com/alias/first_name>", pathValue.stringValue());
		
	}
	
	@Test
	public void testToValueMethod() throws Exception {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Shape shape = new Shape(shapeId);
		shape.addType(SH.Shape);
		
		IriTemplate iriTemplate = new IriTemplate("http://example.com/person/{person_id}");
		shape.setIriTemplate(iriTemplate);
		
		Graph graph = new MemoryGraph();
		
		
		SimplePojoEmitter emitter = new SimplePojoEmitter();
		EmitContext context = new EmitContext(graph);
		
		emitter.emit(context, shape, graph);
		
		Vertex v = graph.getVertex(shapeId);
		
		Value actual = v.getValue(Konig.iriTemplate);
		assertTrue(actual != null);
		
		assertEquals("<http://example.com/person/{person_id}>", actual.stringValue());
		
	}

	@Test
	public void test()  throws Exception {
		
		Graph graph = new MemoryGraph();

		graph.setNamespaceManager(new MemoryNamespaceManager());
		graph.getNamespaceManager().add("schema", "http://schema.org/");
		
		graph.edge(Schema.name, RDF.TYPE, OWL.DATATYPEPROPERTY);
		graph.edge(Schema.address, RDF.TYPE, OWL.OBJECTPROPERTY);
		graph.edge(Schema.contactType, RDF.TYPE, RDF.PROPERTY);
		graph.edge(Schema.addressLocality, RDF.TYPE, RDF.PROPERTY);
		graph.edge(Schema.addressRegion, RDF.TYPE, RDF.PROPERTY);
		graph.edge(Schema.streetAddress, RDF.TYPE, OWL.DATATYPEPROPERTY);
		
		
		
		EmitContext context = new EmitContext(graph);
		SimplePojoEmitter emitter = new SimplePojoEmitter();
		URI personId = uri("http://example.com/alice");
		TestPerson person = new TestPerson();
		person.setId(personId);
		person.setName("Alice Jones");
		TestAddress addressPojo = new TestAddress();
		person.setAddress(addressPojo);
		addressPojo.setStreetAddress("101 Main St.");
		addressPojo.setAddressLocality("Boston");
		addressPojo.setAddressRegion("MA");
		
		Graph sink = new MemoryGraph();
		sink.setNamespaceManager(new MemoryNamespaceManager());
		emitter.emit(context, person, sink);
		
		Vertex alice = sink.getVertex(personId);
		assertTrue(alice!=null);
		
		assertEquals(alice.getValue(Schema.name), literal("Alice Jones"));
		
		Vertex address = alice.getVertex(Schema.address);
		assertEquals(address.getValue(Schema.streetAddress), literal(addressPojo.getStreetAddress()));
		
	}
	
	private Literal literal(String value) {
		return new LiteralImpl(value);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
