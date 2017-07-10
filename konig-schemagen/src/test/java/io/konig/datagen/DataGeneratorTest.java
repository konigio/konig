package io.konig.datagen;

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


import java.io.File;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeLoader;

public class DataGeneratorTest {
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}

	@Test
	public void test() throws Exception {
		
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph();
		RdfUtil.loadTurtle(graph, resource("DataGeneratorTest.ttl"), "");
		Vertex v = graph.v(Konig.SyntheticGraphConstraints).in(RDF.TYPE).firstVertex();
		
		DataGeneratorConfig config = new SimplePojoFactory().create(v, DataGeneratorConfig.class);
		
		File outDir = new File("target/test/datagen/");
		
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		SimpleShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("shapes/Membership-x1.ttl"));
		shapeLoader.loadTurtle(resource("shapes/School-x1.ttl"));
		shapeLoader.loadTurtle(resource("shapes/CourseSection-x1.ttl"));
		
		DataGenerator generator = new DataGenerator(nsManager, shapeManager, mediaTypeNamer);
		generator.generate(config, outDir);
		
	}
	
	private InputStream resource(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
