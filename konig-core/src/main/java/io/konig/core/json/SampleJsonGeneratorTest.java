package io.konig.core.json;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class SampleJsonGeneratorTest {
	private Graph graph = new MemoryGraph(new MemoryNamespaceManager());
	private ShapeManager shapeManager = new MemoryShapeManager();
	private SampleJsonGenerator generator = new SampleJsonGenerator();

	@Test
	public void test() throws Exception {
		load("SampleJsonGeneratorTest/rdf");
		
		URI shapeId = uri("http://example.com/ns/test/KitchenSinkShape");
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		
		generator.generate(shape, out);
		out.close();
		
		System.out.println(buffer.toString());
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String resource) throws Exception {
		File dir = new File( "src/test/resources/" + resource );
		RdfUtil.loadTurtle(dir, graph, shapeManager);
		
	}

}
