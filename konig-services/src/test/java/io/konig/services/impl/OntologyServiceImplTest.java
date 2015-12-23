package io.konig.services.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.VANN;
import io.konig.services.GraphService;
import io.konig.services.KonigConfig;
import io.konig.services.KonigServiceTest;

public class OntologyServiceImplTest extends KonigServiceTest {

	@Test
	public void test() throws Exception {
		KonigConfig config = config();
		GraphService graphService = config.getGraphService();
		

		Graph graph = new MemoryGraph();
		
		Resource activity = graph.vertex(bnode("x1")).getId();
		
		URI alice = uri("http://example.com/alice");
		URI ontology = uri("http://schema.org/");
		URI ontologyHistory = uri("http://schema.org/v/1.0/history");
		
		
		graph.v(activity)
			.addProperty(AS.actor, alice)
			.addProperty(AS.object, ontology);
		
		graph.v(ontology)
			.addLiteral(VANN.preferredNamespacePrefix, "schema")
			.addLiteral(OWL.VERSIONINFO, "1.0");
		

		OntologyServiceImpl service = new OntologyServiceImpl(graphService);
		
		Vertex vertex = graph.vertex(activity);
		service.createOntology(vertex);
		
		Graph ontoGraph = new MemoryGraph();
		graphService.get(ontology, ontoGraph);
		
		

		assertTrue(ontoGraph.v(ontology).hasValue(VANN.preferredNamespacePrefix, "schema").size()==1);
		

		Graph history = new MemoryGraph();
		graphService.get(ontologyHistory, history);
		
		Vertex aliceVertex = history.getVertex(alice);
		assertTrue(aliceVertex != null);
		Vertex activityVertex = aliceVertex.asTraversal().in(AS.actor).firstVertex();
		assertTrue(activityVertex != null);
		
		assertTrue(activityVertex.asTraversal().hasValue(AS.object, ontology).size()==1);
		
		
	}

}
