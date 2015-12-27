package io.konig.core;

/**
 * An exception that occurs if a resource is expected to be named by a URI but 
 * is instead represented as a BNode.
 * 
 * @author Greg McFall
 *
 */
public class UnnamedResourceException extends KonigException {
	private static final long serialVersionUID = 1L;

	public UnnamedResourceException(String message) {
		super(message);
	}

	public UnnamedResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnnamedResourceException(Throwable cause) {
		super(cause);
	}

}
