package io.konig.schemagen;

import org.openrdf.model.URI;

public interface ShapeNamer {

	/**
	 * Generate the name for a Shape that has a given OWL class as its scopeClass.
	 * @param scopeClass The OWL Class that defines the scope for the Shape that is being named. 
	 * @return The URI for the Shape
	 */
	public URI shapeName(URI scopeClass) throws SchemaGeneratorException;
}
