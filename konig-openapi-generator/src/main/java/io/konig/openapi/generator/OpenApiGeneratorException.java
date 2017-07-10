package io.konig.openapi.generator;

public class OpenApiGeneratorException extends Exception {
	private static final long serialVersionUID = 1L;

	public OpenApiGeneratorException(String msg) {
		super(msg);
	}

	public OpenApiGeneratorException(Throwable cause) {
		super(cause);
	}

	public OpenApiGeneratorException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
