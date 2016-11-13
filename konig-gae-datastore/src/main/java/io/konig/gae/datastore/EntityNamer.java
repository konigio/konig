package io.konig.gae.datastore;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;

/**
 * A utility that identifies the Datastore Entity type for a given owl:Class
 * @author Greg McFall
 *
 */
public interface EntityNamer {

	/**
	 * Get the name of the Google Datastore entity that stores entities of a given type.
	 * @param owlClass  The URI for the OWL Class of interest
	 * @return The name of Google Datastore entity that stores instances of the given OWL Class
	 */
	String entityName(URI owlClass);
}
