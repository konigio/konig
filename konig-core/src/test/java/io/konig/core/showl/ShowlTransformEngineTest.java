package io.konig.core.showl;

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


import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShowlTransformEngineTest {
	
	private Graph graph;
	private ShapeManager shapeManager;
	private ShowlTransformEngine engine;
	private Consumer consumer;
	private ShowlService showlService;
	

	@Before
	public void setUp() {

		graph = new MemoryGraph();
		shapeManager = new MemoryShapeManager();
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		Set<URI> targetSystems = Collections.singleton(uri("http://example.com/ns/sys/WarehouseOperationalData"));
		showlService = new ShowlServiceImpl(reasoner);
		ShowlNodeShapeBuilder builder = new ShowlNodeShapeBuilder(showlService, showlService);
		
		ShowlTargetNodeShapeFactory targetNodeShapeFactory = new ReceivesDataFromTargetNodeShapeFactory(targetSystems, graph, builder);
		ShowlSourceNodeFactory sourceNodeFactory = new ReceivesDataFromSourceNodeFactory(builder, graph);
		ShowlTransformService transformService = new BasicTransformService(showlService, showlService, sourceNodeFactory);
		consumer = new Consumer();
		engine = new ShowlTransformEngine(targetNodeShapeFactory, shapeManager, transformService, consumer);
	}
	
	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}
	
	static class Consumer implements ShowlNodeShapeConsumer {
		private List<ShowlNodeShape> list = new ArrayList<>();

		@Override
		public void consume(ShowlNodeShape node) throws ShowlProcessingException {
			list.add(node);
		}

		public List<ShowlNodeShape> getList() {
			return list;
		}
	}

	@Test
	public void test() throws Exception {
		
		List<ShowlNodeShape> result = run("src/test/resources/BasicTransformServiceTest/tabular-mapping");
		assertEquals(1, result.size());
	}

	private List<ShowlNodeShape> run(String path) throws Exception {
		File file = new File(path);
		assertTrue(file.isDirectory());
		
		RdfUtil.loadTurtle(file, graph, shapeManager);

		ShowlClassProcessor classProcessor = new ShowlClassProcessor(showlService, showlService);
		classProcessor.buildAll(shapeManager);
		
		engine.run();
		return consumer.getList();
		
	}

}
