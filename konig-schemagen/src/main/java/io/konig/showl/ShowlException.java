package io.konig.showl;

public class ShowlException extends Exception {
	private static final long serialVersionUID = 1L;

	public ShowlException() {
	}

	public ShowlException(String message) {
		super(message);
	}

	public ShowlException(Throwable cause) {
		super(cause);
	}

	public ShowlException(String message, Throwable cause) {
		super(message, cause);
	}

}
