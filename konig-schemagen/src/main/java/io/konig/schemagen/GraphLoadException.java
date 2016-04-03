package io.konig.schemagen;

public class GraphLoadException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GraphLoadException(String message) {
		super(message);
	}

	public GraphLoadException(Throwable cause) {
		super(cause);
	}
	
	public GraphLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
