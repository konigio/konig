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

import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.SimplePojoEmitter;
import io.konig.core.vocab.CADL;
import io.konig.core.vocab.Schema;

public class CubeTest {

	@Test
	public void test() throws Exception {
		URI cubeId = uri("http://example.com/cube/ProductCostCube");
		URI productVar = uri("http://example.com/cube/ProductCostCube/source/product");
		URI timeDim =  uri("http://example.com/cube/ProductCostCube/dimension/timeDim");
		URI dayLevel = uri("http://example.com/cube/ProductCostCube/dimension/timeDim/level/day");
		URI monthLevel = uri("http://example.com/cube/ProductCostCube/dimension/timeDim/level/monthLevel");
		URI avg =  uri("http://example.com/cube/ProductCostCube/measure/averageCost");
		URI cost =  uri("http://example.com/ns/cost");
	
		Level month = Level.builder()
			.id(monthLevel)
			.formula("DATE_TRUNC(MONTH, ?product.dateCreated)", productVar, Schema.dateCreated)
			.build();
		Cube cube = Cube.builder()
			.id(cubeId)
			.source(Variable.builder()
				.id(productVar)
				.valueType(Schema.Product)
				.build())
			.dimension(Dimension.builder()
				.id(timeDim)
				.level(Level.builder()
					.id(dayLevel)
					.formula("DATE_TRUNC(DAY, ?product.dateCreated)", productVar, Schema.dateCreated)
					.rollUpTo(month)
					.build())
				.level(month)
				.build())
			.measure(Measure.builder()
				.id(avg)
				.formula("SUM(?product.cost)", productVar, cost)
				.build())
			.build();
		
		SimplePojoEmitter emitter = new SimplePojoEmitter();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add(CADL.PREFERRED_NAMESPACE_PREFIX, CADL.NAMESPACE);
		MemoryGraph graph = new MemoryGraph(nsManager);
		
		emitter.emit(cube, graph);
		
		RdfUtil.prettyPrintTurtle(cubeId.stringValue() + "/", graph, new OutputStreamWriter(System.out));
		
		CubeLoader loader = new CubeLoader();
		CubeManager manager = new CubeManager();
		loader.load(graph, manager);
		
		Cube actual = manager.findById(cubeId);
		
		assertTrue(actual != null);
		assertEquals(1, actual.getDimension().size());
		
		Dimension aDim = cube.getDimension().iterator().next();
		Dimension bDim = actual.getDimension().iterator().next();
		assertEquals(aDim.getId(), bDim.getId());
		
		Set<Level> aLevels = aDim.getLevel();
		Set<Level> bLevels = bDim.getLevel();
		
		assertEquals(2, aLevels.size());
		Iterator<Level> aSequence = aLevels.iterator();
		Iterator<Level> bSequence = bLevels.iterator();
		
		Level aLevel = aSequence.next();
		Level bLevel = bSequence.next();
		assertEquals(aLevel.getId(), dayLevel);
		assertTrue(bLevel.getFormula() != null);
		assertEquals(aLevel.getFormula().getText(), bLevel.getFormula().getText());
		assertEquals(aLevel.getRollUpTo().size(), 1);
		assertEquals(aLevel.getRollUpTo().iterator().next().getId(), monthLevel);
		

	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
