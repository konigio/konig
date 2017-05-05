package io.konig.datacatalog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class PathFactoryTest {
	
	@Test 
	public void testRelativePath() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("ex", "http://example.com/ns/");
		nsManager.add("schema", "http://schema.org/");
		Graph graph = new MemoryGraph(nsManager);

		URI category = uri("http://example.com/ns/Category");
		URI target = uri("http://example.com/ns/Electronics/Phone");
		
		graph.edge(category, RDF.TYPE, RDFS.CLASS);
		graph.edge(category, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.CreativeWork, RDF.TYPE, OWL.CLASS);
		
		
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		PathFactory factory = new PathFactory(reasoner, nsManager);
		
		String actual = factory.relativePath(target, Schema.CreativeWork);
		assertEquals("../../../schema/classes/CreativeWork.html", actual);
		
	}

	@Test
	public void testPagePath() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("ex", "http://example.com/ns/");
		nsManager.add("schema", "http://schema.org/");
		Graph graph = new MemoryGraph(nsManager);

		URI category = uri("http://example.com/ns/Category");
		URI target = uri("http://example.com/ns/Electronics/Phone");
		
		graph.edge(category, RDF.TYPE, RDFS.CLASS);
		graph.edge(category, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(target, RDF.TYPE, category);
		
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		PathFactory factory = new PathFactory(reasoner, nsManager);
		
		String path = factory.pagePath(target);
		assertEquals("ex/individuals/Electronics/Phone.html", path);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}


}
