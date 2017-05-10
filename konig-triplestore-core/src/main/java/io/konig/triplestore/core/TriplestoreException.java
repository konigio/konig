package io.konig.triplestore.core;

public class TriplestoreException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public TriplestoreException(String msg) {
		super(msg);
	}
	
	public TriplestoreException(Throwable cause) {
		super(cause);
	}

}
