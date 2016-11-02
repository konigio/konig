package io.konig.gae.datastore;

import org.openrdf.model.Literal;

public interface DatastoreDatatypeMapper {

	/**
	 * Determine the appropriate DatastoreDatatype for a given Literal
	 * @param value The Literal whose DatastoreDatatype is to be determined
	 * @return The DatastoreDatatype for the Literal value
	 */
	DatastoreDatatype getDatastoreDatatype(Literal value);
}
