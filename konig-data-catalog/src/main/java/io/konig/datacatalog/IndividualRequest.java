package io.konig.datacatalog;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class IndividualRequest extends PageRequest {

	private Vertex individual;
	private URI enumerationClass;

	public IndividualRequest(PageRequest other, Vertex individual, URI enumerationClass) {
		super(other);
		this.individual = individual;
		this.enumerationClass = enumerationClass;
	}

	public Vertex getIndividual() {
		return individual;
	}


	public URI getEnumerationClass() {
		return enumerationClass;
	}
	
	
}
