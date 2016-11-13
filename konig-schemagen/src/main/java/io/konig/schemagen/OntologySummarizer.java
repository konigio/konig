package io.konig.schemagen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.VANN;
import io.konig.schemagen.domain.DomainManager;
import io.konig.shacl.ShapeManager;

public class OntologySummarizer {
	
	private static final Logger logger = LoggerFactory.getLogger(OntologySummarizer.class);
	
	
	/**
	 * Extract the OWL classes and relationships between those classes from a broader
	 * collection of OWL and SHACL statements.
	 * 
	 * @param source  The source of OWL and SHACL statements from which the domain model will be extracted.
	 * @return The domain model extracted from the source.
	 */
	public Graph domainModel(Graph source, ShapeManager shapeManager) {
		
		Graph sink = new MemoryGraph();
		
		DomainManager manager = new DomainManager(shapeManager, null);
		manager.load(source);
		manager.export(sink);
		
		return sink;
		
	}
	
	public void writePrototypeModel(NamespaceManager nsManager, Graph source, ShapeManager shapeManager, File outFile) throws SchemaGeneratorException {
		Graph sink = new MemoryGraph();
		DomainManager manager = new DomainManager(shapeManager, nsManager);
		manager.load(source);
		manager.exportPrototype(sink);
		
		try {
			FileWriter out = new FileWriter(outFile);
			try {
				writeTurtle(nsManager, sink, out);
			} finally {
				close(out);
			}
		} catch (IOException e) {
			throw new SchemaGeneratorException(e);
		}
	}
	
	public void writeDomainModel(NamespaceManager nsManager, Graph source, ShapeManager shapeManager, File outFile) throws SchemaGeneratorException {
		Graph sink = domainModel(source, shapeManager);
		
		try {
			FileWriter out = new FileWriter(outFile);
			try {
				writeTurtle(nsManager, sink, out);
			} finally {
				close(out);
			}
		} catch (IOException e) {
			throw new SchemaGeneratorException(e);
		}
	}
	
	
	public void writeTurtle(NamespaceManager nsManager, Graph graph, Writer writer) throws SchemaGeneratorException {
		TurtleWriterFactory factory = new TurtleWriterFactory();
		RDFWriter turtle = factory.getWriter(writer);
		turtle.getWriterConfig().set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true);

		try {
			turtle.startRDF();
			writeNamespaces(nsManager, turtle);
			writeStatements(graph, turtle);
			turtle.endRDF();
			writer.flush();
		} catch (RDFHandlerException | IOException e) {
			throw new SchemaGeneratorException(e);
		}
		
	}

	private void writeStatements(Graph graph, RDFWriter turtle) throws RDFHandlerException {
		
		Iterator<Edge> sequence = graph.iterator();
		while (sequence.hasNext()) {
			Edge edge = sequence.next();
			turtle.handleStatement(edge);
		}
	}


	private void writeNamespaces(NamespaceManager nsManager, RDFWriter turtle) throws RDFHandlerException {
		
		List<Namespace> list = new ArrayList<>(nsManager.listNamespaces());
		
		Collections.sort(list, new Comparator<Namespace>(){

			@Override
			public int compare(Namespace a, Namespace b) {
				return a.getPrefix().compareTo(b.getPrefix());
			}});
		
		for (Namespace ns : list) {
			turtle.handleNamespace(ns.getPrefix(), ns.getName());
		}
		
		 
		
	}


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
		
		if (v.outProperty(RDFS.LABEL).isEmpty()) {
			v.addProperty(RDFS.LABEL, literal("Vocabulary Annotation"));
		}
		
		
	}

	private Literal literal(String value) {
		return new KonigLiteral(value, XMLSchema.STRING);
	}
	private URI uri(String name) {
		
		return new URIImpl(name);
	}

}
