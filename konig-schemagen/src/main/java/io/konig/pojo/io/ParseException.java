package io.konig.pojo.io;

public class ParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ParseException(String msg) {
		super(msg);
	}
	
	public ParseException(Throwable cause) {
		super(cause);
	}

}
