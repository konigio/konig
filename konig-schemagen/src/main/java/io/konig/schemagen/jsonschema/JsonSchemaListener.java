package io.konig.schemagen.jsonschema;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.shacl.Shape;

/**
 * A listener that is notified when the JSON Schema for a Shape is generated.
 * @author Greg McFall
 *
 */
public interface JsonSchemaListener {
	
	/**
	 * Receive notification that the JSON Schema for a Shape has been generated
	 * @param shape The Shape for which a JSON Schema was generated
	 * @param schema The JSON Schema specification
	 */
	public void handleJsonSchema(Shape shape, ObjectNode schema);
}
