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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.Path;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.formula.Expression;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeLoaderTest {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeLoader loader = new ShapeLoader(shapeManager);
	
	@Test
	public void testEquivalentPath() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testEquivalentPath.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		PropertyConstraint p = shape.getProperty().get(0);
		
		Path path = p.getEquivalentPath();
		assertTrue(path != null);
		
		List<Step> pathList = path.asList();
		assertEquals(1, pathList.size());
		
		Step step = pathList.get(0);
		assertTrue(step instanceof OutStep);
		
		OutStep out = (OutStep) step;
		
		assertEquals(Schema.givenName, out.getPredicate());
		
	}
	
	@Test
	public void testSequencePath() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testSequencePath.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<PropertyConstraint> list = shape.getProperty();
		assertEquals(1, list.size());
		
		PropertyConstraint p = list.get(0);
		PropertyPath path = p.getPath();
		assertTrue(path instanceof SequencePath);
		SequencePath sequence = (SequencePath) path;
		assertEquals(2, sequence.size());
		PropertyPath element = sequence.get(0);
		assertTrue(element instanceof PredicatePath);
		assertEquals(Schema.address, ((PredicatePath)element).getPredicate());
		element = sequence.get(1);
		assertTrue(element instanceof PredicatePath);
		assertEquals(Schema.addressCountry, ((PredicatePath)element).getPredicate());
		
	}
	
	@Test
	public void testPredicatePath() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testPredicatePath.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape.getPropertyConstraint(Schema.givenName) != null);
	}

	@Test
	public void testMediaType() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testMediaType.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		assertEquals("application/vnd.example.person", shape.getMediaTypeBaseName());
	}

	@Test
	public void testDefaultShapeFor() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testDefaultShapeFor.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		List<URI> appList = shape.getDefaultShapeFor();
		assertTrue(appList != null);

		assertTrue(appList.contains(uri("http://example.com/app/MyCatalog")));
		assertTrue(appList.contains(uri("http://example.com/app/MyShoppingCart")));
	}
	
	@Test
	public void testConstraint() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testConstraint.ttl");
		URI shapeId = uri("http://example.com/shapes/ShapeWithConstraint");
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		List<Expression> constraint = shape.getConstraint();
		assertTrue(constraint!=null);
		assertEquals(2, constraint.size());
		
		Expression expr = constraint.get(0);
		
		String expected = 
			"@term Activity <http://www.w3.org/ns/activitystreams#Activity>\n" + 
			"@term type <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\n" + 
			"\n" + 
			"?x.type = Activity";
		
		String actual = expr.toString();
		assertEquals(expected, actual);
	}
	
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
	
	@Ignore
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
		
		String expected = 
			"@prefix ex: <http://example.com/ns/> .\n" + 
			"@term status <http://example.com/ns/status>\n" + 
			"@term estimatedPoints <http://example.com/ns/estimatedPoints>\n\n" + 
			"(.status = ex:Complete) ? .estimatedPoints : 0";
		
		assertEquals(expected, formula.toString());
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
		
		Set<URI> typeList = shape.getType();
		assertTrue(typeList != null);
		Iterator<URI> sequence = typeList.iterator();
		assertEquals(2, typeList.size());
		assertEquals(SH.Shape, sequence.next());
		assertEquals(OriginShape, sequence.next());
		
		
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
					.addProperty(SH.path, Schema.address)
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
