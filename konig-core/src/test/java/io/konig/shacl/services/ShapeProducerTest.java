package io.konig.shacl.services;

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

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.services.ShapeProducer;

public class ShapeProducerTest  {
	
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private ShapeManager shapeManager = new MemoryShapeManager();
	private IriTemplate shapeIdTemplate = new IriTemplate("http://example.com/shape/{classLocalName}Shape");
	private ShapeProducer generator = new ShapeProducer(nsManager, shapeManager);

	@Test
	public void testIRI() throws Exception {
		Graph graph = loadGraph("ShapeProducerTest/testIRI.ttl");
		
		Vertex person = graph.getVertex(Schema.Person);
		
		Shape shape = generator.produceShape(person, shapeIdTemplate);
		
		assertEquals(uri("http://example.com/shape/PersonShape"), shape.getId());
		
		PropertyConstraint p = shape.getPropertyConstraint(Schema.parent);
		assertTrue(p != null);
		assertEquals(NodeKind.IRI, p.getNodeKind());
		assertEquals(Schema.Person, p.getValueClass());
		
	}
	
	@Test
	public void testBNode() throws Exception {
		Graph graph = loadGraph("ShapeProducerTest/testBNode.ttl");
		
		Vertex person = graph.getVertex(Schema.Person);
		
		Shape shape = generator.produceShape(person, shapeIdTemplate);
		
		assertEquals(uri("http://example.com/shape/PersonShape"), shape.getId());
		
		PropertyConstraint p = shape.getPropertyConstraint(Schema.address);
		assertTrue(p != null);
		assertEquals(NodeKind.BlankNode, p.getNodeKind());
		
		Shape addressShape = p.getShape();
		assertTrue(addressShape != null);
		assertEquals("http://example.com/shape/PersonShape/address", addressShape.getId().stringValue());
		
		p = addressShape.getPropertyConstraint(Schema.addressLocality);
		assertTrue(p != null);
		assertEquals(XMLSchema.STRING, p.getDatatype());
		
		
	}
	
	@Test
	public void testLiteral() throws Exception {
		Graph graph = loadGraph("ShapeProducerTest/testLiteral.ttl");
		
		Vertex person = graph.getVertex(Schema.Person);
		
		Shape shape = generator.produceShape(person, shapeIdTemplate);
		
		assertEquals(uri("http://example.com/shape/PersonShape"), shape.getId());
		
		PropertyConstraint givenName = shape.getPropertyConstraint(Schema.givenName);
		assertTrue(givenName != null);
		assertEquals(XMLSchema.STRING, givenName.getDatatype());
		
		PropertyConstraint birthDate = shape.getPropertyConstraint(Schema.birthDate);
		assertTrue(birthDate != null);
		assertEquals(XMLSchema.DATE, birthDate.getDatatype());
	}
	
	@Test
	public void testMatch() throws Exception {

		Graph graph = loadGraph("ShapeProducerTest/testMatch.ttl");
		
		Vertex person = graph.getVertex(Schema.Person);
		
		Shape shape = generator.produceShape(person, shapeIdTemplate);
		
		assertEquals("http://example.com/shapes/PersonLiteShape", shape.getId().stringValue());
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		Graph graph = new MemoryGraph();
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		return graph;
	}

}
