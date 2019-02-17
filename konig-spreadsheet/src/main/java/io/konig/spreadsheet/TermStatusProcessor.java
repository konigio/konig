package io.konig.spreadsheet;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;

public class TermStatusProcessor {

	private Graph graph;
	
	/**
	 * Create a new TermStatusProcessor.
	 * @param konigDescriber  A service which describes the Konig ontology.  The TermStatusProcessor depends on the
	 * KonigDescriber only as a way to ensure that it runs.
	 * @param graph The graph into which edges will be written.
	 */
	public TermStatusProcessor(KonigDescriber konigDescriber, Graph graph) {
		this.graph = graph;
		graph.edge(Konig.TermStatus, RDF.TYPE, OWL.CLASS);
		graph.edge(Konig.TermStatus, RDFS.SUBCLASSOF, Schema.Enumeration);
		
	}
	
	public void assertStatus(URI term, URI statusValue) {
		if (term != null && statusValue!=null) {
			graph.edge(statusValue, RDF.TYPE, Konig.TermStatus);
			graph.edge(term, Konig.termStatus, statusValue);
		}
	}

}
