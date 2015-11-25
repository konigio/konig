package io.konig.core;

/**
 * The base class for exceptions within the Konig system
 * @author Greg McFall
 */
public class KonigException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public KonigException(String message) {
		super(message);
	}
	
	public KonigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public KonigException(Throwable cause) {
		super(cause);
	}
}
