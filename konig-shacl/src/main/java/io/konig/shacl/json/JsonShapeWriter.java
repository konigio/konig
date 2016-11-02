package io.konig.shacl.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Vertex;
import io.konig.shacl.Shape;

/**
 * An interface for serializing a resource that conforms to a specific Shape as a JSON object.
 * @author Greg McFall
 *
 */
public interface JsonShapeWriter {
	
	/**
	 * Serialize a resource that conforms to a specific Shape as a JSON object
	 * @param subject The resource to be serialized
	 * @param shape The shape of the resource
	 * @param json The JSON generator used to serialize the resource
	 * @throws IOException
	 */
	public void toJson(Vertex subject, Shape shape, JsonGenerator json) throws IOException;
	
	/**
	 * Serialize a resource that conforms to a specific Shape as a Jackson ObjectNode.
	 * @param subject  The resource to be serialized
	 * @param shape The shape of the resource
	 * @return A Jackson ObjectNode representing the JSON structure
	 * @throws IOException 
	 */
	public ObjectNode toJson(Vertex subject, Shape shape) throws IOException;

}
