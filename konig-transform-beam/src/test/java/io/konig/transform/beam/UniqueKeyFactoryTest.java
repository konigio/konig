package io.konig.transform.beam;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Set;

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
import io.konig.core.showl.BasicTransformService;
import io.konig.core.showl.ReceivesDataFromSourceNodeFactory;
import io.konig.core.showl.ReceivesDataFromTargetNodeShapeFactory;
import io.konig.core.showl.ShowlNodeListingConsumer;
import io.konig.core.showl.ShowlNodeShapeBuilder;
import io.konig.core.showl.ShowlService;
import io.konig.core.showl.ShowlServiceImpl;
import io.konig.core.showl.ShowlSourceNodeFactory;
import io.konig.core.showl.ShowlTargetNodeShapeFactory;
import io.konig.core.showl.ShowlTransformEngine;
import io.konig.core.showl.ShowlTransformService;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
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
		

    JCodeModel model = new JCodeModel();
		BeamTypeManager typeManager = new BeamTypeManagerImpl("com.example", reasoner, model, nsManager);
		
		etran = new BeamExpressionTransform(reasoner, typeManager, model, null);
	}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
