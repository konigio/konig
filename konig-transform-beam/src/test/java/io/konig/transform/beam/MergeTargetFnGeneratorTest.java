package io.konig.transform.beam;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.BasicTransformService;
import io.konig.core.showl.ReceivesDataFromSourceNodeFactory;
import io.konig.core.showl.ReceivesDataFromTargetNodeShapeFactory;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlClassProcessor;
import io.konig.core.showl.ShowlEffectiveNodeShape;
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

public class MergeTargetFnGeneratorTest {

	private Graph graph;
	private JCodeModel model;
	private ShapeManager shapeManager;
	private ShowlTransformEngine engine;
	private ShowlService showlService;
	private ShowlNodeListingConsumer consumer;
	private String basePackage = "com.example";
	private NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
	private OwlReasoner reasoner;
	private BeamTypeManager typeManager;
	
	private URI personTargetShapeId;
	
	@Before
	public void setUp() throws Exception {
		
		personTargetShapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		
		
		
		graph = new MemoryGraph(nsManager);
		model = new JCodeModel();
		reasoner = new OwlReasoner(graph);

		Set<URI> targetSystems = Collections.singleton(uri("http://example.com/ns/sys/WarehouseOperationalData"));
		shapeManager = new MemoryShapeManager();
		showlService =  new ShowlServiceImpl(reasoner);
		ShowlNodeShapeBuilder builder = new ShowlNodeShapeBuilder(showlService, showlService);
		ShowlSourceNodeFactory sourceNodeFactory = new ReceivesDataFromSourceNodeFactory(builder, graph);
		ShowlTransformService transformService = new BasicTransformService(showlService, showlService, sourceNodeFactory);
		ShowlTargetNodeShapeFactory targetNodeShapeFactory = new ReceivesDataFromTargetNodeShapeFactory(targetSystems, graph, builder);

		consumer = new ShowlNodeListingConsumer();
		
		
		typeManager = new BeamTypeManagerImpl(basePackage, reasoner, model, nsManager);
		engine = new ShowlTransformEngine(targetNodeShapeFactory, shapeManager, transformService, consumer);
		
		
		model._class("com.example.common.ErrorBuilder");
		
		
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
		
		Map<ShowlEffectiveNodeShape, IJExpression> tupleTagMap = mockTupleTagMap(node);

		MergeTargetFnGenerator generator = new MergeTargetFnGenerator(tupleTagMap, basePackage, nsManager, model, reasoner, typeManager);
		generator.generate(node);
		

    File javaDir = new File("target/test/TargetFnGeneratorTest/" + rdfDir.getName() + "/src/main/java");
    FileUtils.deleteDirectory(javaDir);
    javaDir.mkdirs();
    
    model.build(javaDir);
		
	}

	private Map<ShowlEffectiveNodeShape, IJExpression> mockTupleTagMap(ShowlNodeShape node) throws JClassAlreadyExistsException {
		JDefinedClass mainClass = model._class(basePackage + ".mock.MainClass");
		Map<ShowlEffectiveNodeShape, IJExpression> map = new HashMap<>();
		for (ShowlChannel channel : node.getChannels()) {
			ShowlEffectiveNodeShape sourceNode = channel.getSourceNode().effectiveNode();
			if (!map.containsKey(sourceNode)) {
				String sName = RdfUtil.shortShapeName(sourceNode.canonicalNode().getId()) + "Tag";
				IJExpression var = mainClass.staticRef(sName);
				map.put(sourceNode, var);
			}
		}
		return map;
	}

	@Ignore
	public void testSystime() throws Exception {
		
		generate("src/test/resources/BeamTransformGeneratorTest/systime", personTargetShapeId);
		
	}

	@Ignore
	public void testJoinNestedRecordViaInverse() throws Exception {
		
		generate("src/test/resources/BeamTransformGeneratorTest/join-nested-record-via-inverse", personTargetShapeId);
		
	}

	@Ignore
	public void testJoinNestedRecord() throws Exception {
		
		generate("src/test/resources/BeamTransformGeneratorTest/join-nested-record", personTargetShapeId);
		
	}

	@Ignore
	public void testJoinById() throws Exception {
		
		generate("src/test/resources/BeamTransformGeneratorTest/join-by-id", uri("http://example.com/shapes/BqPersonShape"));
		
	}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}


}
