package io.konig.transform.beam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
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
import io.konig.core.showl.ShowlClassProcessor;
import io.konig.core.showl.ShowlNodeListingConsumer;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlService;
import io.konig.core.showl.ShowlServiceImpl;
import io.konig.core.util.IOUtil;
import io.konig.datasource.DataSourceManager;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class UniqueKeyFactoryTest {

	private NamespaceManager nsManager;
	private Graph graph;
	private ShapeManager shapeManager;
	private OwlReasoner reasoner;
	private ShowlNodeListingConsumer consumer;
	private ShowlService showlService;
	private BeamExpressionTransform etran;
	
	private UniqueKeyFactory keyFactory;
	
	
	@Before
	public void setUp() {
		nsManager = new MemoryNamespaceManager();
		graph = new MemoryGraph(nsManager);
		shapeManager = new MemoryShapeManager();
		reasoner = new OwlReasoner(graph);
		consumer = new ShowlNodeListingConsumer();
		showlService = new ShowlServiceImpl(reasoner);

    JCodeModel model = new JCodeModel();
		BeamTypeManager typeManager = new BeamTypeManagerImpl("com.example", reasoner, model, nsManager);
		
		etran = new BeamExpressionTransform(reasoner, typeManager, model, null);
		
		keyFactory = new UniqueKeyFactory(etran);
	}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}
	
	public ShowlNodeShape loadNode(String path, URI nodeShapeId) throws Exception {
		
		DataSourceManager.getInstance().clear();
		
		File rdfDir = new File(path);
		assertTrue(rdfDir.exists());
		
		GcpShapeConfig.init();
		RdfUtil.loadTurtle(rdfDir, graph, shapeManager);

		ShowlClassProcessor classProcessor = new ShowlClassProcessor(showlService, showlService);
		classProcessor.buildAll(shapeManager);
		
		
		File projectDir = new File("target/test/UniqueKeyFactoryTest/" + rdfDir.getName());		

		IOUtil.recursiveDelete(projectDir);
	
		Shape shape = shapeManager.getShapeById(nodeShapeId);
		
		return showlService.createNodeShape(shape);
		
	}
	
	@Test
	public void test() throws Exception {
		URI shapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		ShowlNodeShape node = loadNode("src/test/resources/UniqueKeyFactoryTest/merge-by-key", shapeId);
		assertTrue(node != null);
		
		BeamUniqueKeyCollection keyCollection = keyFactory.createKeyList(node);
		assertTrue(keyCollection.size()==1);
		
		BeamUniqueKey key = keyCollection.get(0);
		assertTrue(key.size()==1);
		
		URI expectedPredicate = uri("http://example.com/ns/core/identifiedBy");
		ShowlPropertyShape identifiedBy = key.get(0).getPropertyShape();
		assertEquals(expectedPredicate, identifiedBy.getPredicate());
		
		keyCollection = keyFactory.createKeyList(identifiedBy.getValueShape());
		assertTrue(keyCollection.size()==1);
		
		
	}

}
