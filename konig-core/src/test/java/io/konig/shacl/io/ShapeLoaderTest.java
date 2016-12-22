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


import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeLoaderTest {

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
