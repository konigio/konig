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


import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.helger.jcodemodel.JCodeModel;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ExplicitDerivedFromSelector;
import io.konig.core.showl.MappingStrategy;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeListingConsumer;
import io.konig.core.util.IOUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BeamTransformGeneratorTest {
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private MappingStrategy strategy = new MappingStrategy();
	private ShowlNodeListingConsumer consumer = new ShowlNodeListingConsumer(strategy);
	private ShowlManager showlManager = new ShowlManager(shapeManager, reasoner, new ExplicitDerivedFromSelector(), consumer);
	private JCodeModel model = new JCodeModel();
	
	private BeamTransformGenerator generator = new BeamTransformGenerator("com.example.beam.etl", reasoner);

	@Ignore
	public void testTabularMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/tabular-mapping");
		
	}
	
	@Ignore
	public void testLongMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/long-mapping");
		
	}
	
	@Ignore
	public void testEnumMapping() throws Exception {
		
		generateAll("src/test/resources/BeamTransformGeneratorTest/enum-mapping");
		
	}
	
	public void generateAll(String path) throws Exception {
		
		File rdfDir = new File(path);
		assertTrue(rdfDir.exists());
		
		GcpShapeConfig.init();
		RdfUtil.loadTurtle(rdfDir, graph, shapeManager);
		
		showlManager.load();
		
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
	}
	
	@Ignore
	public void testAll() throws Exception {
		String rdfPath = "src/test/resources/BeamTransformGeneratorTest/tabular-mapping";
		
		File rdfDir = new File(rdfPath);
		GcpShapeConfig.init();
		RdfUtil.loadTurtle(rdfDir, graph, shapeManager);
		
		showlManager.load();
		
		File projectDir = new File("target/test/BeamTransformGenerator/testAll");		

		IOUtil.recursiveDelete(projectDir);
		
		BeamTransformRequest request = BeamTransformRequest.builder()
				.groupId("com.example")
				.artifactBaseId("example")
				.version("1.0")
				.projectDir(projectDir)
				.nodeList(consumer.getList())
				.build();
		
		generator.generateAll(request);
	}

	
	
}
