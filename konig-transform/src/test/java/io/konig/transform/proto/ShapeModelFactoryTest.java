package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.OwlReasoner;
import io.konig.shacl.MemoryPropertyManager;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformTest;

public class ShapeModelFactoryTest extends TransformTest {
	
	private ShapeModelFactory factory;
	private MemoryPropertyManager propertyManager = new MemoryPropertyManager();

	@Before
	public void setUp() {
		DataChannelFactory dataChannelFactory = new DefaultDataChannelFactory();
		OwlReasoner reasoner = new OwlReasoner(graph);
		factory = new ShapeModelFactory(shapeManager, propertyManager, dataChannelFactory, reasoner);
	}

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {

		super.load(path);
		propertyManager.scan(shapeManager);
		
	}

	@Test
	public void testInverseProperty() throws Exception {
		

		load("src/test/resources/konig-transform/inverse-property");
		URI ownerName = iri("http://example.com/ns/ex/ownerName");

		
		URI shapeId = iri("http://example.com/shapes/ProductShape");
		ShapeModel shapeModel = createShapeModel(shapeId);
		
		PropertyModel ownerNameModel = shapeModel.getPropertyByPredicate(ownerName);
		assertTrue(ownerNameModel != null);
		
		assertTrue(ownerNameModel instanceof DirectPropertyModel);
		DirectPropertyModel ownerNameDirect = (DirectPropertyModel) ownerNameModel;
		StepPropertyModel ownerNameTail = ownerNameDirect.getPathTail();
		
		assertTrue(ownerNameTail != null);
		
		PropertyGroup ownerNameTailGroup = ownerNameTail.getGroup();
		assertEquals(2, ownerNameTailGroup.size());
		
		ClassModel classModel = shapeModel.getClassModel();
		assertTrue(classModel != null);
		
		List<SourceShapeInfo> candidateList = classModel.getSourceShapeInfo();
		
		assertTrue(candidateList!=null);
		assertEquals(1, candidateList.size());
	}


	private ShapeModel createShapeModel(URI shapeId) throws ShapeTransformException {
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		return factory.createShapeModel(shape);
	}

}
