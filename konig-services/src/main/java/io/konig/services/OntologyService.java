package io.konig.services;

import io.konig.core.Vertex;

public interface OntologyService {
	
	/**
	 * Create a new Ontology
	 * @param activity A vertex representing a CreateOntology activity.
	 */
	void createOntology(Vertex activity) throws StorageException;

}
