package io.konig.shacl.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ExplicitDerivedFromFilter;
import io.konig.core.showl.HasDataSourceTypeSelector;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlRootTargetClassSelector;
import io.konig.core.showl.ShowlSourceNodeSelector;
import io.konig.core.showl.ShowlTargetNodeSelector;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeFilter;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShowlManagerTest {
	
	private Graph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShowlTargetNodeSelector targetNodeSelector = new HasDataSourceTypeSelector(Konig.GoogleBigQueryTable);
	private ShowlSourceNodeSelector sourceNodeSelector = new ShowlRootTargetClassSelector();
	
	private ShowlManager showlManager = new ShowlManager(shapeManager, reasoner, targetNodeSelector, sourceNodeSelector, null);

	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		File file = new File(path);
		
		assertTrue(file.isDirectory());
		
		RdfUtil.loadTurtle(file, graph, shapeManager);
		showlManager.load();
	}
	
	@Test
	public void testTabularMapping() throws Exception {
		load("src/test/resources/ShowlManagerTest/tabular-mapping");
		
		URI shapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		ShowlNodeShape targetNode = showlManager.getNodeShape(shapeId);
		
		ShowlDirectPropertyShape givenName = targetNode.getProperty(Schema.givenName);
		
		ShowlExpression e = givenName.getSelectedExpression();
		assertTrue(e instanceof ShowlPropertyExpression);
		
		ShowlPropertyExpression propertyExpression = (ShowlPropertyExpression) e;
		ShowlPropertyShape sourceProperty = propertyExpression.getSourceProperty();
		
		assertEquals("first_name", sourceProperty.getPredicate().getLocalName());
		
	}
	
	
}
