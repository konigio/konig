package io.konig.schemagen;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.turtle.TurtleParser;

import io.konig.core.Graph;
import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class OntologySummarizerTest {

	@Ignore
	public void testSummarize() throws Exception {
		
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
	
	@Test
	public void testDomainModel() throws Exception {
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("owl", "http://www.w3.org/2002/07/owl#");
		
		Graph graph = new MemoryGraph();
		new GraphBuilder(graph)
			.beginSubject(Schema.Person)
				.addProperty(RDF.TYPE, OWL.CLASS)
			.endSubject()
			.beginSubject("http://example.com/shapes/v1/schema/Organization")
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Organization)
			.endSubject()
			.beginSubject(Schema.memberOf)
				.addProperty(RDF.TYPE, OWL.OBJECTPROPERTY)
				.addProperty(RDFS.DOMAIN, Schema.Person)
				.addProperty(RDFS.RANGE, Schema.Organization)
			.endSubject()
			.beginSubject("http://example.com/shapes/v1/schema/Person") 
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Person)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.parent)
					.addProperty(SH.valueClass, Schema.Person)
				.endSubject()
			.endSubject()
			;
		
		OntologySummarizer summarizer = new OntologySummarizer();
		
		StringWriter writer = new StringWriter();

		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(null, shapeManager);
		loader.load(graph);
		
		Graph domainModel = summarizer.domainModel(graph, shapeManager);
		
		summarizer.writeTurtle(nsManager, domainModel, writer);
		
		String text = writer.toString();
		
		System.out.println(text);
		
		assertTrue(text.contains("@prefix schema: <http://schema.org/> ."));
		assertTrue(text.contains("schema:Person a owl:Class ;"));
		assertTrue(text.contains("schema:Organization a owl:Class ."));
		
		
	}

	private URI uri(String value) {
		
		return new URIImpl(value);
	}

}
