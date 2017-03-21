package io.konig.spreadsheet;

public class FunctionParseException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public FunctionParseException() {
		
	}
	public FunctionParseException(String msg) {
		super(msg);
	}
	
	public FunctionParseException(Throwable cause) {
		super(cause);
	}

}
