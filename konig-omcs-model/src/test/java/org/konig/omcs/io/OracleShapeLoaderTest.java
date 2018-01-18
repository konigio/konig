package org.konig.omcs.io;

/*
 * #%L
 * Konig Core
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.datasource.DataSource;
import io.konig.omcs.datasource.OracleShapeConfig;
import io.konig.omcs.datasource.OracleTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class OracleShapeLoaderTest {
	
	@Before
	public void setUp() {
		OracleShapeConfig.init();
	}

	@Test
	public void testOmcsDatasources() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/shape_CreativeWorkShape.ttl");

		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		URI shapeId = uri("http://example.com/shapes/CreativeWorkShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertTrue(list != null);
		assertEquals(1, list.size());
		
	
	}
	
	
	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph();
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			RdfUtil.loadTurtle(graph, input, "");
		} finally {
			IOUtil.close(input, resource);
		}
		return graph;
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
