package io.konig.shacl;

import org.openrdf.model.URI;

import io.konig.core.UnnamedResourceException;

public interface ShapeManager {
	
	Shape getShapeById(URI shapeId);
	
	/**
	 * Add a Shape to this manager.
	 * @param shape The shape being added.
	 * @throws UnnamedResourceException If the shape is identified by a BNode.
	 */
	void addShape(Shape shape) throws UnnamedResourceException;

}
