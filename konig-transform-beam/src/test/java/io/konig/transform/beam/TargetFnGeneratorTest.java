package io.konig.transform.beam;

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


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.helger.jcodemodel.JCodeModel;

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
import io.konig.datasource.DataSourceManager;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class TargetFnGeneratorTest {

	private Graph graph;
	private JCodeModel model;
	private ShapeManager shapeManager;
	private ShowlTransformEngine engine;
	private ShowlService showlService;
	private ShowlNodeListingConsumer consumer;
	private SimpleTargetFnGenerator generator;
	
	private URI personTargetShapeId;
	
	@Before
	public void setUp() throws Exception {
		
		personTargetShapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		
		String basePackage = "com.example";
		
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		graph = new MemoryGraph(nsManager);
		model = new JCodeModel();
		OwlReasoner reasoner = new OwlReasoner(graph);

		Set<URI> targetSystems = Collections.singleton(uri("http://example.com/ns/sys/WarehouseOperationalData"));
		shapeManager = new MemoryShapeManager();
		showlService =  new ShowlServiceImpl(reasoner);
		ShowlNodeShapeBuilder builder = new ShowlNodeShapeBuilder(showlService, showlService);
		ShowlSourceNodeFactory sourceNodeFactory = new ReceivesDataFromSourceNodeFactory(builder, graph);
		ShowlTransformService transformService = new BasicTransformService(showlService, showlService, sourceNodeFactory);
		ShowlTargetNodeShapeFactory targetNodeShapeFactory = new ReceivesDataFromTargetNodeShapeFactory(targetSystems, graph, builder);

		consumer = new ShowlNodeListingConsumer();
		
		
		BeamTypeManager typeManager = new BeamTypeManagerImpl(basePackage, reasoner, model, nsManager);
		engine = new ShowlTransformEngine(targetNodeShapeFactory, shapeManager, transformService, consumer);
		
		generator = new SimpleTargetFnGenerator(basePackage, nsManager, model, reasoner, typeManager);
		
		model._class("com.example.common.ErrorBuilder");
		
		
	}


	@Test
	public void testRepeatedRecord() throws Exception {
		
		generate("src/test/resources/BeamTransformGeneratorTest/repeated-record", personTargetShapeId);
		
	}
	
	@Ignore
	public void testJoinById() throws Exception {

		model._class("com.example.schema.GenderType");
		generate("src/test/resources/BeamTransformGeneratorTest/iri-template-formula", personTargetShapeId);
		
	}
	
	@Ignore
	public void testIriTemplateFormula() throws Exception {

		model._class("com.example.schema.GenderType");
		generate("src/test/resources/BeamTransformGeneratorTest/iri-template-formula", personTargetShapeId);
		
	}
	
	@Ignore
	public void testHardCodedEnum() throws Exception {

		model._class("com.example.schema.GenderType");
		generate("src/test/resources/BeamTransformGeneratorTest/hard-coded-enum", personTargetShapeId);
		
	}
	
	@Ignore
	public void testFloatMapping() throws Exception {

		model._class("com.example.schema.GenderType");
		generate("src/test/resources/BeamTransformGeneratorTest/float-mapping", personTargetShapeId);
		
	}

	@Ignore
	public void testEnumIriReference() throws Exception {

		model._class("com.example.schema.GenderType");
		generate("src/test/resources/BeamTransformGeneratorTest/enum-iri-reference", personTargetShapeId);
		
	}

	@Ignore
	public void testClassIriTemplate() throws Exception {
		
		generate("src/test/resources/BeamTransformGeneratorTest/class-iri-template", personTargetShapeId);
		
	}

	@Ignore
	public void testTargetCaseStatement() throws Exception {

		model._class("com.example.ex.Genus");
		model._class("com.example.ex.Species");
		URI shapeId = uri("http://example.com/ns/shape/AnimalTargetShape");
		generate("src/test/resources/BeamTransformGeneratorTest/target-case-statement", shapeId);
	}

	@Ignore
	public void testAnnotatedIdentity() throws Exception {
		generate("src/test/resources/BeamTransformGeneratorTest/annotated-identity", personTargetShapeId);
	}

	@Ignore
	public void testDateMapping() throws Exception {
		generate("src/test/resources/BeamTransformGeneratorTest/date-mapping", personTargetShapeId);
	}
	
	@Ignore
	public void testTabularMapping() throws Exception {
		generate("src/test/resources/BeamTransformGeneratorTest/tabular-mapping", personTargetShapeId);
	}

	@Ignore
	public void testEnumMapping() throws Exception {
		model._class("com.example.schema.GenderType");
		generate("src/test/resources/BeamTransformGeneratorTest/enum-mapping", personTargetShapeId);
	}
	
	public void generate(String path, URI shapeId) throws Exception {
		
		DataSourceManager.getInstance().clear();
		
		File rdfDir = new File(path);
		assertTrue(rdfDir.exists());
		
		GcpShapeConfig.init();
		RdfUtil.loadTurtle(rdfDir, graph, shapeManager);

		ShowlClassProcessor classProcessor = new ShowlClassProcessor(showlService, showlService);
		classProcessor.buildAll(shapeManager);
		
		engine.run();
		
		ShowlNodeShape node = consumer.findById(shapeId);
		assertTrue(node!=null);
		
		generator.generate(node);
		

    File javaDir = new File("target/test/TargetFnGeneratorTest/" + rdfDir.getName() + "/src/main/java");
    FileUtils.deleteDirectory(javaDir);
    javaDir.mkdirs();
    
    model.build(javaDir);
		
}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}


}
