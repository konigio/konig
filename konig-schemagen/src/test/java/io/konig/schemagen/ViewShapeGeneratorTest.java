package io.konig.schemagen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * #%L
 * Konig Schema Generator
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
import java.io.InputStream;

import org.apache.maven.model.FileSet;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.maven.IriTemplateConfig;
import io.konig.maven.TabularShapeGeneratorConfig;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ViewShapeGeneratorTest {
	
	private ShapeManager shapeMgr = new MemoryShapeManager();
	private ShapeLoader shapeLoader = new ShapeLoader(shapeMgr);

	@Test
	public void test() throws Exception {
	
		AwsShapeConfig.init();
		TabularShapeGeneratorConfig config = new TabularShapeGeneratorConfig();
		IriTemplateConfig iriTemplateConfig = new IriTemplateConfig();
		config.setPropertyNamespace("http://schema.org/");
		iriTemplateConfig.setIriPattern("(.*)View$");
		iriTemplateConfig.setIriReplacement("http://example.com/shapes/$1Shape");
		config.setViewIriTemplate(iriTemplateConfig);
		FileSet[] viewFiles = new FileSet[1];
		FileSet fileset = new FileSet();
		fileset.setDirectory("src/test/resources/view-shape-generator/view");
		viewFiles[0] = fileset;
		config.setSqlFiles(viewFiles);
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("shape", "http://example.com/shapes/");
		nsManager.add("sh", "http://www.w3.org/ns/shacl#");
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsManager.add("konig", "http://www.konig.io/ns/core/");
		nsManager.add("xsd", "http://www.w3.org/2001/XMLSchema#");
		
		ShapeManager shapeManager = loadShapes("view-shape-generator/shape_OriginAccountShape.ttl");
		File outDir = new File("target/test/view-shape-generator");
		TabularShapeGenerator tabularShapeGenerator = new TabularShapeGenerator(nsManager, shapeManager);
		tabularShapeGenerator.generateTabularShapes(outDir, config);
		MemoryGraph graph = new MemoryGraph();
		RdfUtil.loadTurtle(new File("target/test/view-shape-generator"), graph, nsManager);
		
		shapeLoader.load(graph);
		
		
		URI shapeId = uri("http://example.com/shapes/AccountShape");
		System.out.println(shapeId+" shapeId");
		Shape shape = shapeMgr.getShapeById(shapeId);
			assertTrue(shape!=null);
			URI name = uri("http://schema.org/name");
			PropertyConstraint nameconstraint = shape.getPropertyConstraint(name);
		assertNotNull(nameconstraint);
		assertEquals(nameconstraint.getPredicate(),uri("http://schema.org/name"));
		assertEquals(nameconstraint.getDatatype(),uri("http://www.w3.org/2001/XMLSchema#string"));
	}
	private URI uri(String value) {
		return new URIImpl(value);
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
