package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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

import org.junit.Ignore;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.vocab.Schema;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class RdbmsShapeGeneratorTest extends AbstractRdbmsShapeGeneratorTest {
	
	@Test
	public void testFlatten() throws Exception {
		load("src/test/resources/nested-entity");
		
		
		URI shapeId = iri("http://example.com/shapes/PersonShape");
		
		
		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		
		assertTrue(rdbmsShape != null);
		
		assertTrue(rdbmsShape.getPropertyConstraint(Schema.givenName) == null);
		
		URI GIVEN_NAME = iri(ALIAS + "ADDRESS__STREET_ADDRESS");
		PropertyConstraint p = rdbmsShape.getPropertyConstraint(GIVEN_NAME);
		assertTrue(p != null);
		
		QuantifiedExpression formula = p.getFormula();
		assertTrue(formula != null);
		
		String text = formula.getText();
		assertEquals(".address.streetAddress", text);
	}
	
	
	


}
