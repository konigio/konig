package io.konig.spreadsheet;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class WorkbookProcessorImplServiceTest {

	@Test
	public void testService() throws Exception {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph(nsManager);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		WorkbookProcessorImpl processor = new WorkbookProcessorImpl(graph, shapeManager, null);
		
		SimpleLocalNameService localNameService = processor.getServiceManager().service(SimpleLocalNameService.class);
		
		graph.edge(Schema.name, RDF.TYPE, RDF.PROPERTY);
		processor.executeDeferredActions();
		
		assertTrue(localNameService != null);
		Set<URI> set = localNameService.lookupLocalName("name");
		assertEquals(1, set.size());
		assertEquals(Schema.name, set.iterator().next());
		
		
	}

}
