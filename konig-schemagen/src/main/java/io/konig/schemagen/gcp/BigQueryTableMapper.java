package io.konig.schemagen.gcp;

import io.konig.core.Vertex;


public interface BigQueryTableMapper {
	
	/**
	 * Get the tableId for the canonical BigQueryTable that holds entities of a given type.
	 * @param owlClass The type of entities for which a tableId is requested.
	 * @return The tableId for the given owlClass or null if there is no such table.
	 */
	String tableForClass(Vertex owlClass);

}
