package io.konig.transform.beam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * #%L
 * Konig Transform Beam
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


import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.BasicTransformService;
import io.konig.core.showl.CompositeSourceNodeFactory;
import io.konig.core.showl.DestinationTypeTargetNodeShapeFactory;
import io.konig.core.showl.ExplicitDerivedFromSourceNodeFactory;
import io.konig.core.showl.ReceivesDataFromSourceNodeFactory;
import io.konig.core.showl.ShowlClassProcessor;
import io.konig.core.showl.ShowlNodeListingConsumer;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlNodeShapeBuilder;
import io.konig.core.showl.ShowlService;
import io.konig.core.showl.ShowlServiceImpl;
import io.konig.core.showl.ShowlTransformEngine;
import io.konig.core.showl.ShowlTransformService;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BeamTransformGeneratorDebug {



	private NamespaceManager nsManager;
	private Graph graph;
	private ShapeManager shapeManager;
	private OwlReasoner reasoner;
	private ShowlNodeListingConsumer consumer;
	private ShowlTransformEngine engine;
	private ShowlService showlService;
	private BeamTransformGenerator generator;
	private PipelineConfig pipelineConfig = new PipelineConfig();

	

	@Before
	public void setUp() {
		pipelineConfig.setCaseInsensitiveEnumLookup(true);
		nsManager = new MemoryNamespaceManager();
		graph = new MemoryGraph(nsManager);
		shapeManager = new MemoryShapeManager();
		reasoner = new OwlReasoner(graph);
		consumer = new ShowlNodeListingConsumer();
		
		showlService = new ShowlServiceImpl(reasoner);

		ShowlNodeShapeBuilder builder = new ShowlNodeShapeBuilder(showlService, showlService);
		DestinationTypeTargetNodeShapeFactory targetNodeFactory = new DestinationTypeTargetNodeShapeFactory(
				Collections.singleton(Konig.GoogleBigQueryTable), builder);
		CompositeSourceNodeFactory sourceNodeFactory = new CompositeSourceNodeFactory();
		sourceNodeFactory.add(new ExplicitDerivedFromSourceNodeFactory(builder));
		sourceNodeFactory.add(new ReceivesDataFromSourceNodeFactory(builder, graph));
		

		ShowlTransformService transformService = new BasicTransformService(showlService, showlService, sourceNodeFactory);
		
		
		engine = new ShowlTransformEngine(targetNodeFactory, shapeManager, transformService, consumer);
		
		ShowlExpressionBuilder expressionBuilder = new ShowlExpressionBuilder(showlService, showlService);
		generator =  new BeamTransformGenerator(pipelineConfig, "com.example.beam.etl", showlService, expressionBuilder);
	}

	public static void main(String[] arg) throws Exception {
		BeamTransformGeneratorDebug debug = new BeamTransformGeneratorDebug();
		debug.setUp();
		debug.test();
	}
	
	@Test
	public void test() throws Exception {
		generateAll("src/test/resources/BeamTransformGeneratorDebug/rdf", false);
		
	}
	
  

	public void generateAll(String path) throws Exception {
		generateAll(path, true);
	}
	
	public void generateAll(String path, boolean withValidation) throws Exception {
		
		File rdfDir = new File(path);
		assertTrue(rdfDir.exists());
		
		GcpShapeConfig.init();
		RdfUtil.loadTurtle(rdfDir, graph, shapeManager);

		ShowlClassProcessor classProcessor = new ShowlClassProcessor(showlService, showlService);
		classProcessor.buildAll(shapeManager);
		
		engine.run();
		
		File projectDir = new File("target/test/BeamTransformGeneratorDebug/" + rdfDir.getName());		

		IOUtil.recursiveDelete(projectDir);
		
		BeamTransformRequest request = BeamTransformRequest.builder()
				.groupId("com.example")
				.artifactBaseId("example")
				.version("1.0")
				.projectDir(projectDir)
				.nodeList(consumer.getList())
				.build();
		
		generator.generateAll(request);
		
		if (withValidation) {
			
			assertTrue(!consumer.getList().isEmpty());
		
			assertTrue(!consumer.getList().isEmpty());
			for (ShowlNodeShape targetNodeShape : consumer.getList()) {
				URI shapeId = RdfUtil.uri(targetNodeShape.getId());
				File actualDir = request.projectDir(shapeId);
				
				if (actualDir.getName().contains("source")) {
					// This is a temporary hack to allow over-generation.
					// TODO: eliminate the over-generation.
					continue;
				}
	
				File expectedDir = new File(rdfDir, actualDir.getName());
				validate(expectedDir, actualDir);
			}
		}
		
	}

	private void validate(File expectedDir, File actualDir) throws IOException {
		if (!expectedDir.exists()) {
			fail("Directory not found: " + expectedDir.getPath());
		}
		
		for (File expectedFile : expectedDir.listFiles()) {
			
			File actualFile = new File(actualDir, expectedFile.getName());
		
			if (!actualFile.exists()) {
				fail("File not found: " + actualFile.getPath());
			}
			assertTrue(actualFile.exists());
			assertEquals(expectedFile.isDirectory(), actualFile.isDirectory());
			if (expectedFile.isDirectory()) {
				validate(expectedFile, actualFile);
			} else {
				
				
				String expectedText = IOUtil.stringContent(expectedFile).trim().replace("\r", "");
				String actualText = IOUtil.stringContent(actualFile).trim().replace("\r", "");
				
				assertEquals(expectedText, actualText);
				
			}
			
		}
		
	}
	

	
	
}
