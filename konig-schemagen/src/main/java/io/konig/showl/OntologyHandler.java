package io.konig.showl;

import org.openrdf.model.URI;

import io.konig.core.Graph;

public interface OntologyHandler {
	
	void handleOwlOntology(URI ontologyId, Graph ontologyGraph) throws ShowlException;
	void handleShapeOntologies(Graph shapeOntologies) throws ShowlException;

}
