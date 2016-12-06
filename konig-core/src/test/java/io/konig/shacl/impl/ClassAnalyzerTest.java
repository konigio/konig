package io.konig.shacl.impl;

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

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;

public class ClassAnalyzerTest {
	
	@Test
	public void testAggregateHierarchy() {

		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.Place, 		 RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.LocalBusiness, RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.LocalBusiness, RDFS.SUBCLASSOF, Schema.Place);
		
		URI placeShapeId = uri("http://example.com/shape/v1/schema/Place");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(placeShapeId) 
			.targetClass(Schema.Place)
			.beginProperty(Schema.name)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
			.beginProperty(Schema.containedInPlace)
				.minCount(0)
				.valueShape(placeShapeId)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		
		OwlReasoner owlReasoner = new OwlReasoner(graph);
		ClassAnalyzer analyzer = new ClassAnalyzer(shapeManager, owlReasoner);
		
		Shape businessShape = analyzer.aggregate(Schema.LocalBusiness);
		AndConstraint and = businessShape.getAnd();
		
		assertTrue(and != null);
		List<Shape> list = and.getShapes();
		assertTrue(list != null);
		assertEquals(1, list.size());
		
		Shape placeShape = list.get(0);
		assertEquals(Schema.Place, placeShape.getTargetClass());
		
		PropertyConstraint p = placeShape.property(Schema.name);
		assertTrue(p != null);
		assertEquals(new Integer(0), p.getMinCount());
		assertEquals(new Integer(1), p.getMaxCount());
		assertEquals(XMLSchema.STRING, p.getDatatype());

		p = placeShape.property(Schema.containedInPlace);
		assertTrue(p != null);
		assertEquals(Schema.Place, p.getValueClass());
		assertEquals(new Integer(0), p.getMinCount());
		assertTrue(p.getMaxCount() == null);
		
	}
	
	@Test
	public void testSimpleMerge() {
		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.Place, 		 RDF.TYPE, OWL.CLASS);
		
		URI placeShapeId = uri("http://example.com/shape/v1/schema/Place");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(placeShapeId) 
			.targetClass(Schema.Place)
			.beginProperty(Schema.name)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
			.beginProperty(Schema.containedInPlace)
				.minCount(0)
				.valueShape(placeShapeId)
			.endProperty()
		.endShape();
		ShapeManager shapeManager = builder.getShapeManager();
		
		OwlReasoner owlReasoner = new OwlReasoner(graph);
		ClassAnalyzer analyzer = new ClassAnalyzer(shapeManager, owlReasoner);
		
		Shape shape = analyzer.aggregate(Schema.Place);
		analyzer.merge(shape);
		PropertyConstraint p = shape.property(Schema.name);

		assertTrue(p != null);
		assertEquals(new Integer(0), p.getMinCount());
		assertEquals(new Integer(1), p.getMaxCount());
		assertEquals(XMLSchema.STRING, p.getDatatype());

		p = shape.property(Schema.containedInPlace);
		assertTrue(p != null);
		assertEquals(Schema.Place, p.getValueClass());
		assertEquals(new Integer(0), p.getMinCount());
		assertTrue(p.getMaxCount() == null);
	}

	@Test
	public void testPullDown() {
		
		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.Place, 		 RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.Organization,  RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.LocalBusiness, RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.LocalBusiness, RDFS.SUBCLASSOF, Schema.Place);
		graph.edge(Schema.LocalBusiness, RDFS.SUBCLASSOF, Schema.Organization);
		
		URI placeShapeId = uri("http://example.com/shape/v1/schema/Place");
		URI orgShapeId = uri("http://example.com/shape/v1/schema/Organization");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(placeShapeId) 
			.targetClass(Schema.Place)
			.beginProperty(Schema.name)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
			.beginProperty(Schema.containedInPlace)
				.minCount(0)
				.valueShape(placeShapeId)
			.endProperty()
		.endShape()
		.beginShape(orgShapeId)
			.targetClass(Schema.Organization)
			.beginProperty(Schema.name)
				.minCount(1)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
			

			.beginProperty(Schema.legalName)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()			
		
		.endShape();
		ShapeManager shapeManager = builder.getShapeManager();
		
		OwlReasoner owlReasoner = new OwlReasoner(graph);
		ClassAnalyzer analyzer = new ClassAnalyzer(shapeManager, owlReasoner);
		
		Shape businessShape = analyzer.aggregate(Schema.LocalBusiness);
		AndConstraint and = businessShape.getAnd();
		
		assertTrue(and != null);
		List<Shape> list = and.getShapes();
		assertTrue(list != null);
		assertEquals(2, list.size());
		
		analyzer.pullDown(businessShape);
		
		assertTrue(businessShape.getAnd() == null);
		PropertyConstraint p = businessShape.property(Schema.name);
		assertTrue(p != null);
		assertEquals(new Integer(1), p.getMinCount());
		assertEquals(new Integer(1), p.getMaxCount());
		assertEquals(XMLSchema.STRING, p.getDatatype());
		
		p = businessShape.property(Schema.containedInPlace);
		assertTrue(p != null);
		assertEquals(Schema.Place, p.getValueClass());
		assertEquals(new Integer(0), p.getMinCount());
		assertTrue(p.getMaxCount() == null);
		
		p = businessShape.property(Schema.legalName);
		assertTrue(p != null);
		assertEquals(new Integer(0), p.getMinCount());
		assertEquals(new Integer(1), p.getMaxCount());
		assertEquals(XMLSchema.STRING, p.getDatatype());
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
