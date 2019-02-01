package io.konig.core.showl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class MappingStrategyTest {

	private ShowlManager showlManager;
	private NamespaceManager nsManager;
	private MappingStrategy strategy = new MappingStrategy();

	@Test
	public void testTabular() throws Exception {
		load("src/test/resources/MappingStrategyTest/tabular");
		URI shapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		ShowlNodeShape node = showlManager.getNodeShape(shapeId).findAny();
		
		List<ShowlDirectPropertyShape> remains = strategy.selectMappings(node);
		
		ShowlPropertyShape givenName = node.findProperty(Schema.givenName);
		
		ShowlMapping mapping = givenName.getSelectedMapping();
		
		assertTrue(mapping != null);
		assertEquals("first_name", mapping.findOther(givenName).getPredicate().getLocalName());
		
		ShowlPropertyShape id = node.findProperty(Konig.id);
		mapping = id.getSelectedMapping();
		assertTrue(mapping != null);
		
		assertTrue(remains.isEmpty());
		
		assertEquals(1, node.getSelectedJoins().size());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String filePath) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(filePath);
		nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph(nsManager);
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
		showlManager = new ShowlManager(shapeManager, reasoner);
		showlManager.load();
		
	}

}
