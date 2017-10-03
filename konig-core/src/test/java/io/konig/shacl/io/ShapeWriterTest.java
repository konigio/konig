package io.konig.shacl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import java.io.File;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.activity.Activity;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.FileGetter;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class ShapeWriterTest {
	
	private ShapeWriter shapeWriter = new ShapeWriter();
	private Graph graph = new MemoryGraph();
	
	@Test
	public void testSequencePath() throws Exception {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Shape shape = new Shape(shapeId);
		
		SequencePath path = new SequencePath();
		path.add(new PredicatePath(Schema.address));
		path.add(new PredicatePath(Schema.addressCountry));
		
		PropertyConstraint p = new PropertyConstraint();
		p.setPath(path);
		
		shape.add(p);
		
		graph.setNamespaceManager(MemoryNamespaceManager.getDefaultInstance());
		shapeWriter.emitShape(shape, graph);
		
		Vertex v = graph.getVertex(shapeId);
		
		Vertex property = v.getVertex(SH.property);
		Vertex list = property.getVertex(SH.path);
		
		List<Value> javaList = list.asList();
		assertEquals(2, javaList.size());
		assertEquals(Schema.address, javaList.get(0));
		assertEquals(Schema.addressCountry, javaList.get(1));
		
		RdfUtil.prettyPrintTurtle(graph, System.out);
	}
	
	@Test
	public void testIdFormat() throws Exception {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Shape shape = new Shape(shapeId);
		shape.setIdFormat(Konig.Curie);
		
		shapeWriter.emitShape(shape, graph);
		assertStatement(shapeId, Konig.idFormat, Konig.Curie);
	}
	
	private void assertStatement(Resource subject, URI predicate, Value object) {
		assertTrue(graph.contains(subject, predicate, object));
	}

	@Test
	public void testFormula() throws Exception {

		URI shapeId = uri("http://example.com/IssueShape");
		URI completedPoints = uri("http://example.com/ns/completedPoints");
		Shape shape = new Shape(shapeId);
		PropertyConstraint p = new PropertyConstraint(completedPoints);
		shape.add(p);
		p.setFormula(new QuantifiedExpression("(status = ex:Complete) ? estimatedPoints : 0"));
		
		shapeWriter.emitShape(shape, graph);

		Vertex v = graph.getVertex(shapeId);
		assertTrue(v!=null);
		
		Vertex w = v.getVertex(SH.property);
		assertTrue(w != null);
		
		assertLiteral(w, Konig.formula, "(status = ex:Complete) ? estimatedPoints : 0");
		
	}

	private void assertLiteral(Vertex u, URI predicate, String expected) {
		Value v = u.getValue(predicate);
		assertTrue("Failed to get value '" + predicate.getLocalName() + "'", v != null);
		assertEquals(expected, v.stringValue());
	
		
	}

	@Test
	public void test() throws Exception {
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		File baseDir = new File("target/test/ShapeWriterTest");
		
		nsManager.add("ex", "http://example.com/shape/");
		
		URI personShapeId = uri("http://example.com/shape/PersonShape");
		URI genderTypeShape = uri("http://example.com/shape/GenderTypeShape");
		
		Activity activity = new Activity();
		activity.setId(Activity.nextActivityId());
		activity.setType(Konig.GenerateEnumTables);
		activity.setEndTime(GregorianCalendar.getInstance());
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
		.endShape()
		.beginShape(genderTypeShape)
			.wasGeneratedBy(activity)
		.endShape();
		
		FileGetter fileGetter = new ShapeFileGetter(baseDir, nsManager);
		
		Set<URI> activityWhitelist = new HashSet<>();
		activityWhitelist.add(Konig.GenerateEnumTables);
		Collection<Shape> shapeList = builder.getShapeManager().listShapes();
		
		shapeWriter.writeGeneratedShapes(nsManager, shapeList, fileGetter, activityWhitelist);
		
		// TODO: implement assertions
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
