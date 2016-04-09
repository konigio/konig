package io.konig.schemagen;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.turtle.TurtleParser;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.vocab.VANN;

public class OntologySummarizerTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("owl", "http://www.w3.org/2002/07/owl#");
		
		Graph graph = new MemoryGraph();
		
		StringWriter writer = new StringWriter();
		
		OntologySummarizer summarizer = new OntologySummarizer();
		summarizer.summarize(nsManager, graph, writer);
		
		
		Graph loaded = new MemoryGraph();
		GraphLoadHandler loadHandler = new GraphLoadHandler(loaded);
		String text = writer.toString();
		
		StringReader reader = new StringReader(text);
		TurtleParser parser = new TurtleParser();
		parser.setRDFHandler(loadHandler);
		
		parser.parse(reader, "");
		
		Vertex owl = loaded.vertex(uri(OWL.NAMESPACE));
		assertEquals("owl", owl.getValue(VANN.preferredNamespacePrefix).stringValue());
		
		
	}

	private URI uri(String value) {
		
		return new URIImpl(value);
	}

}
