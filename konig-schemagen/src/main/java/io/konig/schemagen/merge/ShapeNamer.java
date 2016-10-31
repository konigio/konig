package io.konig.schemagen.merge;

import org.openrdf.model.URI;

import io.konig.schemagen.SchemaGeneratorException;

public interface ShapeNamer {

	/**
	 * Generate the name for a Shape that has a given OWL class as its targetClass.
	 * @param targetClass The OWL Class that defines the scope for the Shape that is being named. 
	 * @return The URI for the Shape
	 */
	public URI shapeName(URI targetClass) throws SchemaGeneratorException;
}
