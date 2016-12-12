package io.konig.gae.datastore;

import org.openrdf.model.URI;

public interface DaoNamer {
	
	/**
	 * Compute the fully-qualified Java interface name for the DAO that provides access
	 * to entities of a given type.
	 * @param owlClass The OWL Class that specifies the entity type.
	 * @return The Java class name for the DAO
	 */
	String daoName(URI owlClass);

}
