package io.konig.jsonschema.generator;

import io.konig.shacl.Shape;

public interface JsonSchemaNamer {
	
	/**
	 * Compute the id for the JSON Schema associated with a given SHACL Shape.
	 * @param shape The Shape that is associated with the JSON Schema.
	 * @return The fully-qualified URL for the JSON Schema
	 */
	String schemaId(Shape shape);
	

}
