package io.konig.datacatalog;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class IndividualRequest extends PageRequest {

	private Vertex individual;
	private URI enumerationClass;

	public IndividualRequest(PageRequest other) {
		super(other);
	}

	public Vertex getIndividual() {
		return individual;
	}

	public void setIndividual(Vertex individual) {
		this.individual = individual;
	}

	public URI getEnumerationClass() {
		return enumerationClass;
	}

	public void setEnumerationClass(URI enumerationClass) {
		this.enumerationClass = enumerationClass;
	}
	
	
}
