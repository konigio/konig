package io.konig.content;

public class ContentAccessException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ContentAccessException(String message) {
		super(message);
	}
	
	public ContentAccessException(Throwable cause) {
		super(cause);
	}

}
