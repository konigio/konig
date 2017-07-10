package io.konig.transform.factory;

/*
 * #%L
 * Konig Transform
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

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.transform.factory.SourceProperty;
import io.konig.transform.factory.SourceShape;
import io.konig.transform.factory.TargetProperty;
import io.konig.transform.factory.TargetShape;

public class TargetShapeTest extends TransformTest {

	@Test
	public void testMatch() throws Exception {

		load("src/test/resources/konig-transform/org-founder-zipcode");
		URI targetShapeId = iri("http://example.com/shapes/BqOrganizationShape");
		URI sourceShapeId = iri("http://example.com/shapes/OriginOrganizationShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		Shape sourceShape = shapeManager.getShapeById(sourceShapeId);
		
		TargetShape targetNode = TargetShape.create(targetShape);
		SourceShape sourceNode = SourceShape.create(sourceShape);
		
		targetNode.match(sourceNode);
		
		TargetProperty founder = targetNode.getProperty(Schema.founder);
		assertTrue(founder != null);
		
		TargetProperty address = founder.getNestedShape().getProperty(Schema.address);
		assertTrue(address != null);
		
		TargetProperty postalCode = address.getNestedShape().getProperty(Schema.postalCode);
		assertTrue(postalCode != null);
		
		Set<SourceProperty> matchSet = postalCode.getMatches();
		assertEquals(1, matchSet.size());
		
		SourceProperty zipCode = matchSet.iterator().next();
		assertEquals("http://example.com/ns/alias/founder_zipcode", zipCode.getPropertyConstraint().getPredicate().stringValue());
		
	}

}
