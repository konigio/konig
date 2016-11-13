package io.konig.gae.datastore;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.Vertex;

/**
 * A service for storing and retrieving entities.
 * @author Greg McFall
 *
 */
public interface SemanticDatastore {

	/**
	 * Store an entity using its canonical shape.
	 * The canonical shape includes all datatype properties and nested object properties for relationships
	 * to anonymous individuals (i.e. properties whose value is given by a BNode).
	 * @param entity A Vertex containing a description of the entity to be stored
	 */
	void put(Vertex entity);
	
	/**
	 * Load the description of an entity from the datastore.
	 * @param entityId The URI for the entity to be retrieved
	 * @param graph A graph into which the description of the entity will be loaded.
	 * @return A Vertex description of the entity
	 */
	Vertex getById(URI entityId, Graph graph);
}
