package io.konig.etl.aws;

import static org.junit.Assert.assertEquals;

/*
 * #%L
 * konig-etl-generator
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


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class EtlRouteBuilderTest {
	
	ShapeManager shapeManager = new MemoryShapeManager();
	
	@Test
	public void testShapeDependencies() throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		RdfUtil.loadTurtle(new File("src/test/resources/shape-dependencies"), graph, nsManager);
		 
		RdfUtil.loadTurtle(new File("src/test/resources/shapes"), graph, shapeManager);
		
		for (Vertex targetShapeVertex : graph.vertices()) {
			Shape targetShape = shapeManager.getShapeById(new URIImpl("http://example.com/shapes/AuroraPersonShape"));
			if (targetShape.getId().equals(targetShapeVertex.getId()) && targetShape.hasDataSourceType(Konig.AwsAuroraTable)) {
				assertEquals("http://example.com/shapes/AuroraPersonShape", targetShapeVertex.getId().toString());
				List<Vertex> sourceList = targetShapeVertex.asTraversal().out(Konig.DERIVEDFROM).toVertexList();
				assertEquals(2, sourceList.size());
				assertEquals("http://example.com/shapes/PersonShape", sourceList.get(0).getId().toString());
				assertEquals("http://example.com/shapes/OriginPersonShape", sourceList.get(1).getId().toString());
			}
		}
	}
	
	@Test
	public void testEtlMs()
	{
		EtlRouteBuilder builder=new EtlRouteBuilder();
		try {
			builder.createDockerFile("localName","schemaName");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
