package io.konig.schemagen.jsonschema;

import io.konig.shacl.Shape;

public interface JsonSchemaNamer {
	
	/**
	 * Compute the id for the JSON Schema associated with a given SHACL Shape.
	 * @param shape The Shape that is associated with the JSON Schema.
	 * @return The fully-qualified URL for the JSON Schema
	 */
	String schemaId(Shape shape);
	
	/**
	 * Compute the name of the file in which the JSON Schema for a given Shape will be stored.
	 * @param shape The Shape whose JSON Schema is to be stored in a file.
	 * @return The name of the file that will store the JSON Schema for the given Shape
	 */
	String jsonSchemaFileName(Shape shape);
	

}
