package io.konig.shacl;

import io.konig.core.KonigException;

public class ShapeNotFoundException extends KonigException {
	private static final long serialVersionUID = 1L;

	public ShapeNotFoundException(String shapeId) {
		super("Shape not found: " + shapeId);
	}

	public ShapeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShapeNotFoundException(Throwable cause) {
		super(cause);
	}

}
