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

import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VAR;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformTest;
import io.konig.transform.rule.ShapeRule;

public class ShapeModelFactoryTest extends TransformTest {
	
	protected ShapeModelFactory factory;
	
	@Before
	public void setUp() {
		factory = new ShapeModelFactory(shapeManager, null, new OwlReasoner(graph));
	}
	
	

	@Ignore
	public void testAggregateFunction() throws Exception {

		load("src/test/resources/konig-transform/aggregate-function");
		
		URI shapeId = iri("http://example.com/shapes/AssessmentMetricsShape");
		ShapeModel shapeModel = createShapeModel(shapeId);
		
		URI varId = VAR.variableId("?x");
		PropertyModel p = shapeModel.getPropertyByPredicate(varId);
		
		assertTrue(p instanceof VariablePropertyModel);
		
		VariablePropertyModel var = (VariablePropertyModel) p;
		URI sourceShapeId = iri("http://example.com/shapes/AssessmentResultShape");
		
		ShapeModel sourceShape = var.getSourceShape();
		assertTrue(sourceShape != null);
		assertEquals(sourceShapeId, sourceShape.getShape().getId());
		
		
	
	}

	@Ignore
	public void testFieldExactMatch() throws Exception {
		load("src/test/resources/konig-transform/field-exact-match");
		
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeModel shapeModel = createShapeModel(shapeId);
		
		Collection<PropertyModel> propertyList = shapeModel.getProperties();
		assertEquals(1, propertyList.size());
		
		ClassModel classModel = shapeModel.getClassModel();
		
		PropertyGroup group = classModel.getPropertyGroupByPredicate(Schema.givenName);
		assertTrue(group!=null);
		
		assertEquals(2, group.size());
		assertTrue(group.getTargetProperty()!=null);
		assertEquals(shapeId, group.getTargetProperty().getDeclaringShape().getShape().getId());

		URI sourceShapeId = iri("http://example.com/shapes/OriginPersonShape");
		
		assertEquals(sourceShapeId, group.get(1).getDeclaringShape().getShape().getId());
		
	}
	
	@Ignore
	public void testFlattenedField() throws Exception {
		load("src/test/resources/konig-transform/flattened-field");
		
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeModel shapeModel = createShapeModel(shapeId);
		
		ClassModel classModel = shapeModel.getClassModel();
		
		PropertyGroup group = classModel.getPropertyGroupByPredicate(Schema.address);
		assertTrue(group!=null);
		
		assertEquals(2, group.size());
		assertTrue(group.getTargetProperty()!=null);
		
		ClassModel addressModel = group.getValueClassModel();
		assertTrue(addressModel != null);
		
		PropertyGroup postalCodeGroup = addressModel.getPropertyGroupByPredicate(Schema.postalCode);
		assertTrue(postalCodeGroup != null);
		
		assertEquals(2, postalCodeGroup.size());
		URI postalAddressShapeId = iri("http://example.com/shapes/PostalAddressShape");
		
		assertEquals(postalAddressShapeId, postalCodeGroup.getTargetProperty().getDeclaringShape().getShape().getId());

		
	}

	private ShapeModel createShapeModel(URI shapeId) throws ShapeTransformException {
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			fail("Shape not found: " + shapeId);
		}
		
		return factory.createShapeModel(shape);
	}

}
