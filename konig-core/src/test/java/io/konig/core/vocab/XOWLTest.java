package io.konig.core.vocab;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeReasoner;
import io.konig.shacl.impl.MemoryShapeManager;

public class XOWLTest {
	
	private Graph graph = new MemoryGraph();
	private OwlReasoner owlReasoner = new OwlReasoner(graph);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeReasoner shapeReasoner = new ShapeReasoner(shapeManager);
	private ShapeBuilder shapeBuilder = new ShapeBuilder(shapeManager);

	@Test
	public void testDatatypePropertyByRange() {
		
		
		graph.edge(Schema.name, RDF.TYPE, RDF.PROPERTY);
		graph.edge(Schema.name, RDFS.RANGE, XMLSchema.STRING);
		
		XOWL.classifyProperties(owlReasoner, shapeReasoner);
		
		assertTrue(graph.contains(Schema.name, RDF.TYPE, OWL.DATATYPEPROPERTY));
		assertTrue(!graph.contains(Schema.name, RDF.TYPE, OWL.OBJECTPROPERTY));
		
	}
	
	@Test
	public void testObjectPropertyByRange() {
		graph.edge(Schema.address, RDF.TYPE, RDF.PROPERTY);
		graph.edge(Schema.address, RDFS.RANGE, Schema.PostalAddress);

		XOWL.classifyProperties(owlReasoner, shapeReasoner);
		
		assertTrue(!graph.contains(Schema.address, RDF.TYPE, OWL.DATATYPEPROPERTY));
		assertTrue(graph.contains(Schema.address, RDF.TYPE, OWL.OBJECTPROPERTY));
		
		
	}
	
	@Test
	public void testPropertyShapeDatatype() {
		shapeBuilder
		.beginShape()
			.beginProperty(Schema.name)
				.datatype(XMLSchema.STRING)
			.endProperty()
		.endShape();

		XOWL.classifyProperties(owlReasoner, shapeReasoner);
		
		assertTrue(graph.contains(Schema.name, RDF.TYPE, OWL.DATATYPEPROPERTY));
		assertTrue(!graph.contains(Schema.name, RDF.TYPE, OWL.OBJECTPROPERTY));
	}
	
	@Test
	public void testPropertyShapeObject() {

		shapeBuilder
		.beginShape()
			.beginProperty(Schema.address)
				.valueClass(Schema.PostalAddress)
			.endProperty()
		.endShape();

		XOWL.classifyProperties(owlReasoner, shapeReasoner);
		
		assertTrue(!graph.contains(Schema.address, RDF.TYPE, OWL.DATATYPEPROPERTY));
		assertTrue(graph.contains(Schema.address, RDF.TYPE, OWL.OBJECTPROPERTY));
	}
	
	

}
