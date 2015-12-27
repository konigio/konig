package io.konig.core.io;

import io.konig.core.KonigException;

/**
 * An exception which occurs while reading graph data from some source.
 * @author Greg
 *
 */
public class KonigReadException extends KonigException {
	

	private static final long serialVersionUID = 1L;

	public KonigReadException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public KonigReadException(String message) {
		super(message);
	}

	public KonigReadException(Throwable cause) {
		super(cause);
	}


}
