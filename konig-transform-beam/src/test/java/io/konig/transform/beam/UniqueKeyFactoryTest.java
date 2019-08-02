package io.konig.transform.beam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

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
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.ShowlUniqueKeyCollection;
import io.konig.core.showl.UniqueKeyFactory;
import io.konig.core.util.IOUtil;
import io.konig.datasource.DataSourceManager;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.transform.beam.BeamTypeManager;
import io.konig.transform.beam.BeamTypeManagerImpl;

public class UniqueKeyFactoryTest {

	private NamespaceManager nsManager;
	private Graph graph;
	private ShapeManager shapeManager;
	private OwlReasoner reasoner;
	private ShowlService showlService;
	
	private UniqueKeyFactory keyFactory;
	
	
	@Before
	public void setUp() {
		nsManager = new MemoryNamespaceManager();
		graph = new MemoryGraph(nsManager);
		shapeManager = new MemoryShapeManager();
		reasoner = new OwlReasoner(graph);
		showlService = new ShowlServiceImpl(reasoner);

		
		
		keyFactory = new UniqueKeyFactory(reasoner);
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
		
		ShowlUniqueKeyCollection keyCollection = keyFactory.createKeyCollection(node);
		assertTrue(keyCollection.size()==1);
		
		ShowlUniqueKey key = keyCollection.get(0);
		assertTrue(key.size()==1);
		
		URI expectedPredicate = uri("http://example.com/ns/core/identifiedBy");
		ShowlPropertyShape identifiedBy = key.get(0).getPropertyShape();
		assertEquals(expectedPredicate, identifiedBy.getPredicate());
		
		ShowlUniqueKeyCollection valueKeys = key.get(0).getValueKeys();
		assertTrue(valueKeys != null);
		assertTrue(valueKeys.size()==1);
		key = valueKeys.get(0);
		assertEquals(2, key.size());
		
		URI identifier = uri("http://example.com/ns/core/identifier");
		URI identityProvider = uri("http://example.com/ns/core/identityProvider");
		
		assertEquals(identifier, key.get(0).getPropertyShape().getPredicate());
		assertEquals(identityProvider, key.get(1).getPropertyShape().getPredicate());
		
		List<ShowlPropertyShape> flatList = keyCollection.flatten();
		
		assertTrue(flatList != null);
		assertEquals(2, flatList.size());

		assertEquals(identifier, flatList.get(0).getPredicate());
		assertEquals(identityProvider, flatList.get(1).getPredicate());
		
	}

}
