package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class GoogleCloudResourceGeneratorTest {
	
	@Before
	public void setup() {
		GcpShapeConfig.init();
	}

	@Ignore
	public void testBigQueryTable() throws Exception {
		
		ShapeManager shapeManager = loadShapes("GoogleCloudResourceGeneratorTest/testBigQueryTable.ttl");
		File outDir = new File("target/GoogleCloudResourceGeneratorTest");

		File expectedFile = new File(outDir, "schema.Person.json" );
		expectedFile.delete();
		
		GoogleCloudResourceGenerator generator = new GoogleCloudResourceGenerator();
		generator.addBigQueryGenerator(outDir);
		generator.dispatch(shapeManager.listShapes());
		
		assertTrue(expectedFile.exists());
	}
	
	@Test
	public void testSpannerTable() throws Exception {
		
		ShapeManager shapeManager = loadShapes("GoogleCloudResourceGeneratorTest/testSpannerTable.ttl");
		File outDir = new File("target/GoogleCloudResourceGeneratorTest");

		File expectedFile = new File(outDir, "schema.Person.json" );
		expectedFile.delete();
		
		GoogleCloudResourceGenerator generator = new GoogleCloudResourceGenerator();
		generator.addSpannerGenerator(outDir, shapeManager);
		generator.dispatch(shapeManager.listShapes());
		
		assertTrue(expectedFile.exists());
	}

	private ShapeManager loadShapes(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = loadGraph(resource);
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		
		return shapeManager;
	}

	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph();
		InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			RdfUtil.loadTurtle(graph, stream, "");
		} finally {
			IOUtil.close(stream, resource);
		}
		return graph;
	}

}
