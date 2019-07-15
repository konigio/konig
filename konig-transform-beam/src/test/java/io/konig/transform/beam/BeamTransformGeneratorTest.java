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
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.BasicTransformService;
import io.konig.core.showl.ReceivesDataFromSourceNodeFactory;
import io.konig.core.showl.ReceivesDataFromTargetNodeShapeFactory;
import io.konig.core.showl.ShowlClassProcessor;
import io.konig.core.showl.ShowlNodeListingConsumer;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlNodeShapeBuilder;
import io.konig.core.showl.ShowlService;
import io.konig.core.showl.ShowlServiceImpl;
import io.konig.core.showl.ShowlSourceNodeFactory;
import io.konig.core.showl.ShowlTargetNodeShapeFactory;
import io.konig.core.showl.ShowlTransformEngine;
import io.konig.core.showl.ShowlTransformService;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
import io.konig.core.util.IOUtil;
import io.konig.datasource.DataSourceManager;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BeamTransformGeneratorTest {
	private NamespaceManager nsManager;
	private Graph graph;
	private ShapeManager shapeManager;
	private OwlReasoner reasoner;
	private ShowlNodeListingConsumer consumer;
	private ShowlService showlService;

	private ShowlTransformEngine engine;
	private BeamTransformGenerator generator;

	@Before
	public void setUp() {
		nsManager = new MemoryNamespaceManager();
		graph = new MemoryGraph(nsManager);
		shapeManager = new MemoryShapeManager();
		reasoner = new OwlReasoner(graph);
		consumer = new ShowlNodeListingConsumer();
		
		Set<URI> targetSystems = Collections.singleton(uri("http://example.com/ns/sys/WarehouseOperationalData"));
		showlService = new ShowlServiceImpl(reasoner);
		ShowlNodeShapeBuilder builder = new ShowlNodeShapeBuilder(showlService, showlService);
		
		ShowlTargetNodeShapeFactory targetNodeShapeFactory = new ReceivesDataFromTargetNodeShapeFactory(targetSystems, graph, builder);
		ShowlSourceNodeFactory sourceNodeFactory = new ReceivesDataFromSourceNodeFactory(builder, graph);
		ShowlTransformService transformService = new BasicTransformService(showlService, showlService, sourceNodeFactory);
		
		engine = new ShowlTransformEngine(targetNodeShapeFactory, shapeManager, transformService, consumer);
		ShowlExpressionBuilder expressionBuilder = new ShowlExpressionBuilder(showlService, showlService);
		generator =  new BeamTransformGenerator("com.example.beam.etl", showlService, expressionBuilder);
	}


	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}

	@Ignore
	public void testOverlay() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/overlay");
		
	}

	@Ignore
	public void testTrivialIriTemplate() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/trivial-iri-template", false);
		
	}
	

	@Test
	public void testClassIriTemplate() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/class-iri-template");
		
	}

	@Test
	public void testNestedRecord() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/nested-record");
		
	}
	
	@Test
	public void testIriTemplateFormula() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/iri-template-formula", false);
		
	}
	
	@Test
	public void testHardCodedEnum() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/hard-coded-enum", false);
		
	}

//	public void testBeamCube() throws Exception {
//		generateAll("src/test/resources/BeamTransformGeneratorTest/beam-cube");
//		
//	}
	


	@Test
	public void testModelSummary() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/model-summary", false);
		
	}
	
	@Test
	public void testJoinById() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/join-by-id", false);
		
	}
	
	@Test
	public void testDateMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/date-mapping");
		
	}
	

	@Test
	public void testTabularMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/tabular-mapping");
		
	}
	
	@Test
	public void testLongMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/long-mapping");
		
	}
	
	@Test
	public void testFloatMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/float-mapping");
		
	}
	
	@Test
	public void testEnumMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/enum-mapping");
		
	}

	
	@Test
	public void testEnumIriReference() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/enum-iri-reference");
		
	}
	

	@Ignore
	public void testJoinNestedRecord() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/join-nested-record", false);
		
	}


	@Ignore
	public void testJoinNestedRecordViaInverse() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/join-nested-record-via-inverse", false);
		
	}


	@Test
	public void testSystime() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/systime");
		
	}
	
	@Test
	public void testAnnotatedIdentity() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/annotated-identity", false);
		
	}
	
	@Test
	public void testTargetCaseStatement() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/target-case-statement");
		
	}
	
	@Test
	public void testRepeatedRecord() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/repeated-record", false);
		
	}

	public void generateAll(String path) throws Exception {
		generateAll(path, true);
	}
	
	public void generateAll(String path, boolean withValidation) throws Exception {
		
		DataSourceManager.getInstance().clear();
		
		File rdfDir = new File(path);
		assertTrue(rdfDir.exists());
		
		GcpShapeConfig.init();
		RdfUtil.loadTurtle(rdfDir, graph, shapeManager);

		ShowlClassProcessor classProcessor = new ShowlClassProcessor(showlService, showlService);
		classProcessor.buildAll(shapeManager);
		
		engine.run();
		
		File projectDir = new File("target/test/BeamTransformGenerator/" + rdfDir.getName());		

		IOUtil.recursiveDelete(projectDir);
		
		BeamTransformRequest request = BeamTransformRequest.builder()
				.groupId("com.example")
				.artifactBaseId("example")
				.version("1.0")
				.projectDir(projectDir)
				.nodeList(consumer.getList())
				.build();
		
		generator.generateAll(request);
		
		if (generator.isEncounteredError()) {
			fail("BeamTransformGenerator encountered an error (see stack trace in log)");
		}
		
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
