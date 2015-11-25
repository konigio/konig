package io.konig.core.io;

import io.konig.core.KonigException;

public class KonigWriteException extends KonigException {
	private static final long serialVersionUID = 1L;

	public KonigWriteException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public KonigWriteException(String message) {
		super(message);
	}
}
