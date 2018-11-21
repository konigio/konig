package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.jsonpath.JsonPathParseException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class JsonPathProcessorTest {
	
	private String namespace = "http://example.com/alias/";
	private ShapeManager shapeManager = new MemoryShapeManager();
	private JsonPathProcessor processor = new JsonPathProcessor(shapeManager, namespace);
	private URI shapeId;
	private Shape shape;
	
	@Before
	public void setUp() {
		 shapeId = uri("http://example.com/shapes/PersonShape");
		 shape = new Shape(shapeId);
		 shapeManager.addShape(shape);
	}

	@Test
	public void testSimpleField() throws Exception {
		
		PropertyConstraint q = process("$.givenName");
		
		URI predicate = uri(namespace + "givenName");
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		
		assertTrue(p == q);
		assertTrue(p.getMaxCount() != null);
		assertEquals(new Integer(1), p.getMaxCount());
		
	}
	
	@Test
	public void testNestedField() throws Exception {
		PropertyConstraint q = process("$.address[*].postalCode");
		
		URI addressShapeId = uri("http://example.com/shapes/Person.addressShape");
		Shape addressShape = shapeManager.getShapeById(addressShapeId);
		assertTrue(addressShape != null);

		URI predicate = uri(namespace + "postalCode");
		PropertyConstraint p = addressShape.getPropertyConstraint(predicate);
		
		assertTrue(p == q);
		assertTrue(p.getMaxCount() != null);
		assertEquals(new Integer(1), p.getMaxCount());
		
		
		predicate = uri(namespace + "address");
		p = shape.getPropertyConstraint(predicate);
		assertTrue(p != null);
		assertTrue(p.getMaxCount() == null);
		
	}

	private PropertyConstraint process(String jsonPath) throws JsonPathParseException, IOException {
		
		return processor.parse(shape, jsonPath);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
