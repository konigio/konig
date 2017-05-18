package io.konig.shacl;

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

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ShapeValidatorTest {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private ShapeValidator validator = new ShapeValidator();
	private ValidationReport report = new ValidationReport();
	
	@Test
	public void testNodeKind() throws Exception {
		Graph graph = loadGraph("ShapeValidatorTest/testNodeKind.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/address>", result.getPath().toString());
		assertEquals("Expected an IRI but found  a BNode", result.getMessage());
	}
	
	@Test
	public void testEmbeddedShape() throws Exception {
		Graph graph = loadGraph("ShapeValidatorTest/testEmbeddedShape.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/address>/<http://schema.org/addressRegion>", result.getPath().toString());
		assertEquals("Expected at least 1 value but found 0", result.getMessage());
	}
	
	@Test
	public void testExpectResourceButFounLiteral() throws Exception {
		Graph graph = loadGraph("ShapeValidatorTest/testExpectResourceButFoundLiteral.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/address>", result.getPath().toString());
		assertEquals("Value must not be a literal but found '101 Main Street'", result.getMessage());
		
	}

	@Test
	public void testMinCount() throws Exception {
		Graph graph = loadGraph("ShapeValidatorTest/testMinCount.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/familyName>", result.getPath().toString());
		assertEquals("Expected at least 1 value but found 0", result.getMessage());
		
	}


	@Test
	public void testMaxCount() throws Exception {
		Graph graph = loadGraph("ShapeValidatorTest/testMaxCount.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/familyName>", result.getPath().toString());
		assertEquals("Expected at most 1 value but found 2", result.getMessage());
		
	}
	
	@Test
	public void testDatatype() throws Exception {

		Graph graph = loadGraph("ShapeValidatorTest/testDatatype.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/birthDate>", result.getPath().toString());
		assertEquals("Expected value of type xsd:date but found xsd:dateTime", result.getMessage());
	}

	
	@Test
	public void testClosed() throws Exception {

		Graph graph = loadGraph("ShapeValidatorTest/testClosed.ttl");
		
		Vertex alice = graph.getVertex(uri("http://example.com/person/alice"));
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonShape"));
		validator.setClosed(true);
		boolean ok = validator.validate(alice, shape, report);
		
		assertTrue(!ok);
		assertEquals(1, report.getValidationResult().size());
		ValidationResult result = report.getValidationResult().get(0);
		assertEquals("/<http://schema.org/familyName>", result.getPath().toString());
		assertEquals("Property not permitted in closed shape", result.getMessage());
	}

	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph(nsManager);
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		
		return graph;
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
