package io.konig.shacl.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;

import io.konig.core.UnnamedResourceException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class MemoryShapeManager implements ShapeManager {
	private Map<String, Shape> shapeMap = new HashMap<String, Shape>();

	public Shape getShapeById(URI shapeId) {
		return shapeMap.get(shapeId.stringValue());
	}

	public void addShape(Shape shape) throws UnnamedResourceException {
		
		if (shape.getId() instanceof BNode) {
			throw new UnnamedResourceException("Cannot add unnamed Shape to this manager");
		}
		shapeMap.put(shape.getId().stringValue(), shape);
	}

}
