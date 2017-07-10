package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShapeGeneratorTest {
	
	private Graph graph = new MemoryGraph();
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeGenerator generator = new ShapeGenerator(reasoner);

	@Test
	public void test() {
		
		Vertex alice = graph.vertex("http://example.com/person/alice");
		Resource aliceId = alice.getId();
		graph.edge(aliceId, Schema.givenName, literal("Alice"));
		graph.edge(aliceId, Schema.familyName, literal("Jones"));
		
		Vertex bob = graph.vertex("http://example.com/person/bob");
		Vertex address = graph.vertex();
		bob.addProperty(Schema.address, address.getId());
		address.addProperty(Schema.postalCode, literal("20837"));
		
		alice.addProperty(Schema.knows, bob.getId());
		
		Vertex cathy = graph.vertex("http://example.com/person/cathy");
		alice.addProperty(Schema.knows, cathy.getId());
		alice.addProperty(RDF.TYPE, Schema.Person);
		bob.addProperty(RDF.TYPE, Schema.Person);
		cathy.addProperty(RDF.TYPE, Schema.Person);
		
		
		List<Vertex> list = new ArrayList<>();
		list.add(alice);
		list.add(bob);
		Shape shape = generator.generateShape(list);
		
		PropertyConstraint givenName = shape.getPropertyConstraint(Schema.givenName);
		assertTrue(givenName != null);
		assertEquals(new Integer(1), givenName.getMaxCount());
		assertEquals(XMLSchema.STRING, givenName.getDatatype());
		
		PropertyConstraint addressProperty = shape.getPropertyConstraint(Schema.address);
		assertTrue(addressProperty != null);
		
		Shape addressShape = addressProperty.getShape();
		assertTrue(addressShape != null);
		
		PropertyConstraint postalCode = addressShape.getPropertyConstraint(Schema.postalCode);
		assertTrue(postalCode != null);
		
		PropertyConstraint knows = shape.getPropertyConstraint(Schema.knows);
		assertTrue(knows != null);
		
		assertEquals(Schema.Person, knows.getValueClass());
		assertEquals(NodeKind.IRI, knows.getNodeKind());
		
		
		
	}

	private Literal literal(String value) {
		
		return new LiteralImpl(value);
	}

}
