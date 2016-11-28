package io.konig.showl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.extract.ExtractException;
import io.konig.core.extract.OntologyExtractor;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.FileGetter;
import io.konig.core.vocab.SH;

public class OntologyWriter {
	
	public static URI SHAPE_NAMESPACE_FILE = new URIImpl("urn:konig:shapeNamespaceFile");

	private FileGetter fileGetter;
	private OntologyExtractor extractor;
	
	public OntologyWriter(FileGetter fileGetter) {
		this.fileGetter = fileGetter;
		extractor = new OntologyExtractor();
	}

	public void writeOntologies(Graph graph) throws IOException, KonigException {
		Set<String> shapeNamespaces = extractor.shapeNamespaces(graph);
		
		List<Vertex> list = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
		for (Vertex v : list) {
			writeOntology(v, shapeNamespaces);
		}
		
		writeShapeNamespaces(graph, shapeNamespaces);
		
	}

	private void writeShapeNamespaces(Graph graph, Set<String> shapeNamespaces) throws IOException {

		if (!shapeNamespaces.isEmpty()) {
			
			List<String> list = new ArrayList<>(shapeNamespaces);
			Collections.sort(list);
			MemoryGraph g = new MemoryGraph();
			g.setNamespaceManager(graph.getNamespaceManager());
			
			extractor.collectShapeOntologies(graph, shapeNamespaces, g);
			
			
			File file = fileGetter.getFile(SHAPE_NAMESPACE_FILE);
			FileWriter writer = new FileWriter(file);
			try {
				RdfUtil.prettyPrintTurtle(g, writer);
			} catch (RDFHandlerException e) {
				throw new KonigException(e);
			} finally {
				writer.close();
			}
		}
		
	}


	private void writeOntology(Vertex v, Set<String> shapeNamespaces) throws IOException {
		
		Resource id = v.getId();
		if (!(id instanceof URI)) {
			throw new KonigException("Ontology must be identified by a URI");
		}
		URI ontologyId = (URI) id;
		
		if (shapeNamespaces.contains(ontologyId.stringValue())) {
			return;
		}
		
		Graph graph = new MemoryGraph();
		graph.setNamespaceManager(v.getGraph().getNamespaceManager());
		
		try {
			extractor.extract(v, graph);
			File file = fileGetter.getFile(ontologyId);
			
			FileWriter writer = new FileWriter(file);
			try {
				RdfUtil.prettyPrintTurtle(graph, writer);
				
			} finally {
				writer.close();
			}
			
			
			
		} catch (ExtractException | RDFHandlerException e) {
			throw new KonigException(e);
		}
		
	}

}
