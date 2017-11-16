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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformTest;

public class SimplePropertyMapperTest extends TransformTest {

	protected ShapeModelFactory factory;
	
	@Before
	public void setUp() {
		factory = new ShapeModelFactory(shapeManager, null, new OwlReasoner(graph));
	}
	
	@Test
	public void testFieldExactMatch() throws Exception {
		load("src/test/resources/konig-transform/field-exact-match");
		
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeModel shapeModel = createShapeModel(shapeId);
		
		PropertyGroup group = shapeModel.getClassModel().getPropertyGroupByPredicate(Schema.givenName);
		PropertyModel sourceProperty = group.getSourceProperty();
		assertTrue(sourceProperty != null);
		assertEquals(Schema.givenName, sourceProperty.getPredicate());
		
	}


	private ShapeModel createShapeModel(URI shapeId) throws ShapeTransformException {
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			fail("Shape not found: " + shapeId);
		}
		
		return factory.createShapeModel(shape);
	}

}
