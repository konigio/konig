package io.konig.runtime.io;

public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ValidationException() {
	}

	public ValidationException(String arg0) {
		super(arg0);
	}

	public ValidationException(Throwable arg0) {
		super(arg0);
	}

	public ValidationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
