package io.konig.shacl.transform;

import io.konig.core.KonigException;

public class InvalidShapeException extends KonigException {
	private static final long serialVersionUID = 1L;

	public InvalidShapeException(String message) {
		super(message);
	}

	public InvalidShapeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidShapeException(Throwable cause) {
		super(cause);
	}

}
