package io.konig.schemagen.gcp;

import java.io.File;

import io.konig.core.Vertex;

public interface DataFileMapper {
	
	/**
	 * Get the File where we should store a JSON document containing records for members of
	 * an enumeration.
	 * @param owlClass The vertex for the OWL Class that defines the set of values.
	 * @return The File containing the JSON document for the given OWL Class.
	 */
	public File fileForEnumRecords(Vertex owlClass);

}
