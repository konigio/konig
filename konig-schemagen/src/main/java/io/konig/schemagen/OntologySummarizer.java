package io.konig.schemagen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.turtle.TurtleWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.KonigLiteral;
import io.konig.core.vocab.VANN;

public class OntologySummarizer {
	
	private static final Logger logger = LoggerFactory.getLogger(OntologySummarizer.class);
	
	public void summarize(NamespaceManager nsManager, Graph input, Writer writer) throws SchemaGeneratorException {

		try {
			declareNamespaces(nsManager, input);
			// Get a list of all vertices
			List<Vertex> list = input.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
			Collections.sort(list, new OntologyComparator());
			
			TurtleWriterFactory factory = new TurtleWriterFactory();
			RDFWriter turtle = factory.getWriter(writer);
			turtle.getWriterConfig().set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true);

			turtle.startRDF();
			for (Vertex v : list) {
				String prefix = v.getValue(VANN.preferredNamespacePrefix).stringValue();
				String namespace = v.getId().stringValue();
				turtle.handleNamespace(prefix, namespace);
			}
			
			for (Vertex v : list) {
				writeOntology(turtle, v);
			}
			
			turtle.endRDF();
			writer.flush();
			
		} catch (RDFHandlerException | IOException e) {
			throw new SchemaGeneratorException(e);
		}
	}
	
	private void writeOntology(RDFWriter turtle, Vertex v) throws RDFHandlerException {
		
		Set<Edge> typeSet = v.outProperty(RDF.TYPE);
		
		for (Edge edge : typeSet) {
			turtle.handleStatement(edge);
		}
		
		Set<Entry<URI, Set<Edge>>> outward = v.outEdges();
		
		List<Entry<URI, Set<Edge>>> list = new ArrayList<>(outward);
		Collections.sort(list, new PropertyComparator());
		
		for (Entry<URI, Set<Edge>> e : list) {
			URI predicate = e.getKey();
			if (predicate.equals(RDF.TYPE)) {
				continue;
			}
			Set<Edge> set = e.getValue();
			for (Edge edge : set) {
				turtle.handleStatement(edge);
			}
		}
		
	}

	public void summarize(NamespaceManager nsManager, Graph input, File outFile) throws SchemaGeneratorException {
		
		
		try {
			FileWriter writer = new FileWriter(outFile);
			try {
				summarize(nsManager, input, writer);
			} finally {
				close(writer);
			}
		} catch (IOException e) {
			throw new SchemaGeneratorException(e);
		}
		
	}
	
	private static class PropertyComparator implements Comparator<Entry<URI, Set<Edge>>> {

		@Override
		public int compare(Entry<URI, Set<Edge>> a, Entry<URI, Set<Edge>> b) {
			
			URI aURI = a.getKey();
			URI bURI = b.getKey();
			
			String aLocal = aURI.getLocalName();
			String bLocal = bURI.getLocalName();
			
			int result = aLocal.compareTo(bLocal);
			if (result == 0) {
				result = aURI.stringValue().compareTo(bURI.stringValue());
			}
			
			return result;
		}
		
	}
	
	private static class OntologyComparator implements Comparator<Vertex> {

		@Override
		public int compare(Vertex a, Vertex b) {
			
			Value aPrefix = a.getValue(VANN.preferredNamespacePrefix);
			Value bPrefix = b.getValue(VANN.preferredNamespacePrefix);
			
			String aValue = aPrefix==null ? a.getId().stringValue() : aPrefix.stringValue();
			String bValue = bPrefix==null ? b.getId().stringValue() : bPrefix.stringValue();
			return aValue.compareTo(bValue);
		}
		
	}

	private void close(Writer writer) {
		try {
			writer.close();
		} catch (Throwable ignore) {
			logger.warn("Failed to close writer", ignore);
		}
		
	}

	private void declareNamespaces(NamespaceManager nsManager, Graph graph) {
		
		declareVann(graph);
		
		Collection<Namespace> list = nsManager.listNamespaces();
		for (Namespace ns : list) {
			URI nsURI = uri(ns.getName());
			Vertex v = graph.vertex(nsURI);
			
			if (v.getValue(VANN.preferredNamespacePrefix) == null) {
				v.addProperty(VANN.preferredNamespacePrefix, literal(ns.getPrefix()));
			}
			
			if (!v.hasProperty(RDF.TYPE, OWL.ONTOLOGY)) {
				v.addProperty(RDF.TYPE, OWL.ONTOLOGY);
			}
			
		}
		
	}

	private void declareVann(Graph graph) {
		
		Vertex v = graph.vertex(VANN.NAMESPACE_URI);
		

		if (v.getValue(VANN.preferredNamespacePrefix) == null) {
			v.addProperty(VANN.preferredNamespacePrefix, literal("vann"));
		}
		
		if (!v.hasProperty(RDF.TYPE, OWL.ONTOLOGY)) {
			v.addProperty(RDF.TYPE, OWL.ONTOLOGY);
		}
		
		if (v.outProperty(RDFS.COMMENT).isEmpty()) {
			v.addProperty(RDFS.COMMENT, literal("A vocabulary for annotating ontologies with examples and usage notes."));
		}
		
		
	}

	private Literal literal(String value) {
		return new KonigLiteral(value, XMLSchema.STRING);
	}
	private URI uri(String name) {
		
		return new URIImpl(name);
	}

}
