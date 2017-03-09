package io.konig.shacl.io;

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
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.formula.Expression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeLoaderTest {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeLoader loader = new ShapeLoader(shapeManager);
	
	@Test
	public void testDerivedProperty() throws Exception {

		Graph graph = loadGraph("ShapeLoaderTest/testDerivedProperty.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		List<PropertyConstraint> derived = shape.getDerivedProperty();
		assertTrue(derived != null);
		assertEquals(1, derived.size());
		PropertyConstraint p =  derived.get(0);
		assertEquals("happinessIndex",p.getPredicate().getLocalName());
		assertEquals("1 + 2 + 3", p.getFormula().toString());
	}
	
	@Test 
	public void testIdFormat() throws Exception {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Graph graph = new MemoryGraph();
		graph.edge(shapeId, RDF.TYPE, SH.Shape);
		graph.edge(shapeId, Konig.idFormat, Konig.Curie);
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertEquals(Konig.Curie, shape.getIdFormat());
		
	}
	
	@Test 
	public void testFormula() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testFormula.ttl");

		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		URI shapeId = uri("http://example.com/shapes/IssueShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		URI completedPoints = uri("http://example.com/ns/completedPoints");
		PropertyConstraint p = shape.getPropertyConstraint(completedPoints);
		Expression formula = p.getFormula();
		assertTrue(formula != null);
		assertEquals("(status = ex:Complete) ? estimatedPoints : 0", formula.toString());
	}


	
	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph();
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			RdfUtil.loadTurtle(graph, input, "");
		} finally {
			IOUtil.close(input, resource);
		}
		return graph;
	}

	
	
	@Test
	public void testType() {
		
		URI shapeId = uri("http://example.com/PersonShape");
		URI OriginShape = uri("http://example.com/ns/OriginShape");
		
		Graph graph = new MemoryGraph();
		
		graph.edge(shapeId, RDF.TYPE, SH.Shape);
		graph.edge(shapeId, RDF.TYPE, OriginShape);
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		List<URI> typeList = shape.getType();
		assertTrue(typeList != null);
		assertEquals(2, typeList.size());
		assertEquals(SH.Shape, typeList.get(0));
		assertEquals(OriginShape, typeList.get(1));
		
		
	}

	@Test
	public void testShape() {
		URI shapeId = uri("http://example.com/shape/PersonShape");
		URI addressShapeId = uri("http://example.com/shape/PostalAddress");
		
		Graph graph = new MemoryGraph();
		
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Person)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.address)
					.addProperty(SH.shape, addressShapeId)
				.endSubject()
			.endSubject()
			.beginSubject(addressShapeId)
				.addProperty(SH.targetClass, Schema.PostalAddress)
			.endSubject();
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		PropertyConstraint constraint = shape.getPropertyConstraint(Schema.address);
		assertTrue(constraint!=null);
		
		Shape addressShape = constraint.getShape();
		assertTrue(addressShape!=null);
		
		assertEquals(addressShape.getTargetClass(), Schema.PostalAddress);
		
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
