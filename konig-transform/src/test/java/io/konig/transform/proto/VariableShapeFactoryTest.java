package io.konig.transform.proto;

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

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.transform.factory.TransformTest;

public class VariableShapeFactoryTest extends TransformTest {
	
	private VariableShapeFactory factory = new VariableShapeFactory();

	@Test
	public void testAggregateFunction() throws Exception {

		load("src/test/resources/konig-transform/aggregate-function");
		URI shapeId = iri("http://example.com/shapes/AssessmentMetricsShape");
		URI resultOf = iri("http://example.com/ns/core/resultOf");
		URI score = iri("http://example.com/ns/core/score");
		URI AssessmentResult = iri("http://example.com/ns/core/AssessmentResult");
		
		Shape focusShape = shapeManager.getShapeById(shapeId);
		
		PropertyConstraint variable = focusShape.getVariableByName("?x");
		
		Shape varShape = factory.createShape(focusShape, variable);
		
		assertEquals(varShape.getTargetClass(), AssessmentResult);
		assertTrue(varShape.getPropertyConstraint(resultOf) != null);
		assertTrue(varShape.getPropertyConstraint(score)!=null);
	}

}
