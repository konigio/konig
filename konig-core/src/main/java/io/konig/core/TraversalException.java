package io.konig.core;

public class TraversalException extends KonigException {
	private static final long serialVersionUID = 1L;

	public TraversalException(String message) {
		super(message);
	}

	public TraversalException(String message, Throwable cause) {
		super(message, cause);
	}

	public TraversalException(Throwable cause) {
		super(cause);
	}

}
