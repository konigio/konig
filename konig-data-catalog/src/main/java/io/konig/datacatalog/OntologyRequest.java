package io.konig.datacatalog;

import io.konig.core.Vertex;

public class OntologyRequest extends PageRequest {

	private Vertex ontologyVertex;
	
	public OntologyRequest(PageRequest other, Vertex ontologyVertex) {
		super(other);
		this.ontologyVertex = ontologyVertex;
	}

	public Vertex getOntologyVertex() {
		return ontologyVertex;
	}


}
