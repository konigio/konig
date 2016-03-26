package io.konig.shacl.io;

/*
 * #%L
 * konig-shacl
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

import java.io.InputStream;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.rio.turtle.TurtleParser;

import com.github.jsonldjava.core.RDFParser;

import io.konig.core.KonigTest;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.io.JsonldParser;
import io.konig.core.io.ListRdfHandler;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Constraint;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeRdfHandlerTest extends KonigTest {

	@Test
	public void runTurtle() throws Exception {
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeRdfHandler handler = new ShapeRdfHandler(shapeManager);
		
		
		TurtleParser parser = new TurtleParser();
		ListRdfHandler listHandler = new ListRdfHandler(handler, handler);
		parser.setRDFHandler(listHandler);
		
		InputStream input = getClass().getClassLoader().getResourceAsStream("shapes/schemaShapes.ttl");

		parser.parse(input, "");
		input.close();
		
		testAssertions(shapeManager);
		
	}
	
	private void testAssertions(ShapeManager shapeManager) throws Exception {
		
		
		Shape person = shapeManager.getShapeById(uri("http://www.konig.io/shape/schema/v1/Person"));
		
		assertTrue(person != null);

		PropertyConstraint givenName = person.getPropertyConstraint(Schema.givenName);
		assertTrue(givenName != null);
		
		assertEquals(new Integer(1), givenName.getMinCount());
		
		PropertyConstraint address = person.getPropertyConstraint(Schema.address);
		assertTrue(address != null);
		
		Shape addressShape = address.getValueShape();
		assertTrue(addressShape != null);
		assertEquals(Schema.PostalAddress, addressShape.getScopeClass());
		
		Shape person2 = shapeManager.getShapeById(uri("http://www.konig.io/shape/schema/v2/Person"));
		assertTrue(person2 != null);
		
		Constraint constraint = person2.getConstraint();
		
		assertTrue(constraint instanceof OrConstraint);
		
		OrConstraint orConstraint = (OrConstraint) constraint;
		
		List<Shape> shapeList = orConstraint.getShapes();
		
		assertTrue(containsName(shapeList));
		assertTrue(containsGivenNameAndFamilyName(shapeList));
	}
	
	@Test
	public void test() throws Exception {
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeRdfHandler handler = new ShapeRdfHandler(shapeManager);
		
		MemoryContextManager contextManager = new MemoryContextManager();
		contextManager.loadFromClasspath();
		
		JsonldParser parser = new JsonldParser(contextManager);
		parser.setRDFHandler(handler);
		
		
		InputStream input = getClass().getClassLoader().getResourceAsStream("shapes/schemaShapes.jsonld");
		parser.parse(input, "");
		input.close();
		
		Shape person = shapeManager.getShapeById(uri("http://www.konig.io/shape/schema/v1/Person"));
		
		assertTrue(person != null);

		PropertyConstraint givenName = person.getPropertyConstraint(Schema.givenName);
		assertTrue(givenName != null);
		
		assertEquals(new Integer(1), givenName.getMinCount());
		
		PropertyConstraint address = person.getPropertyConstraint(Schema.address);
		assertTrue(address != null);
		
		Shape addressShape = address.getValueShape();
		assertTrue(addressShape != null);
		assertEquals(Schema.PostalAddress, addressShape.getScopeClass());
		
		Shape person2 = shapeManager.getShapeById(uri("http://www.konig.io/shape/schema/v2/Person"));
		assertTrue(person2 != null);
		
		Constraint constraint = person2.getConstraint();
		
		assertTrue(constraint instanceof OrConstraint);
		
		OrConstraint orConstraint = (OrConstraint) constraint;
		
		List<Shape> shapeList = orConstraint.getShapes();
		
		assertTrue(containsName(shapeList));
		assertTrue(containsGivenNameAndFamilyName(shapeList));
		
	}

	private boolean containsGivenNameAndFamilyName(List<Shape> shapeList) {
		for (Shape shape : shapeList) {
			int count = 0;
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				if (Schema.givenName.equals(predicate)) {
					count++;
				}
				if (Schema.familyName.equals(predicate)) {
					count++;
				}
				if (count == 2) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean containsName(List<Shape> shapeList) {
		
		for (Shape shape : shapeList) {
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				if (Schema.name.equals(predicate)) {
					return true;
				}
			}
		}
		return false;
	}

}
