package io.konig.spreadsheet;

import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.VANN;

public class KonigDescriber {

	
	public KonigDescriber(Graph graph) {

		graph.edge(Konig.NAMESPACE_ID, RDF.TYPE, OWL.ONTOLOGY);
		graph.edge(Konig.NAMESPACE_ID, VANN.preferredNamespacePrefix, new LiteralImpl("konig"));
	}

}
