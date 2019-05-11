package io.konig.cadl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Schema;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class CubeShapeBuilderTest {
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private CubeManager cubeManager = new CubeManager();
	
	private CubeShapeBuilder builder = new CubeShapeBuilder(
			reasoner,
			shapeManager, 
			"http://example.com/shape/");

	@Test
	public void test() throws Exception {
		
		Cube cube = loadCube(
			uri("http://example.com/cube/OpportunityRevenueCube"), 
			"src/test/resources/CubeBuilderTest/workbook-cube");
		
		Shape shape = builder.buildShape(cube);
		
		assertEquals("http://example.com/ns/crm/Opportunity", shape.getTargetClass().stringValue());
		
		Level accountLevel = cube.findDimensionByName("accountDim").findLevelByName("account");
		
		PropertyConstraint accountConstraint = shape.getPropertyConstraint(CubeUtil.predicate(cube, accountLevel.getId()));
		assertTrue(accountConstraint != null);
		
		QuantifiedExpression formula = accountConstraint.getFormula();
		assertTrue(formula != null);
		assertEquals("$.customerAccount", formula.toSimpleString());
		
		Shape accountShape = accountConstraint.getShape();
		assertTrue(accountShape != null);

		
		Attribute accountNameAttr = accountLevel.findAttributeByName("name");
		PropertyConstraint accountNameConstraint = accountShape.getPropertyConstraint(Schema.name);
//		CubeUtil.predicate(cube, accountNameAttr.getId())
		assertTrue(accountNameConstraint != null);
		
		formula = accountNameConstraint.getFormula();
		assertTrue(formula == null);
		
		assertEquals(1, accountShape.getProperty().size());
		
	}
	
	private Cube loadCube(URI cubeId, String path) throws RDFParseException, RDFHandlerException, IOException {
		load(path);
		return cubeManager.findById(cubeId);
	}
	
	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		File folder = new File(path);
		RdfUtil.loadTurtle(folder, graph, shapeManager);
		CubeLoader loader = new CubeLoader();
		loader.load(graph, cubeManager);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
}
