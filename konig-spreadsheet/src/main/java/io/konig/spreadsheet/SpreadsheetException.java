package io.konig.spreadsheet;

public class SpreadsheetException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public SpreadsheetException(String message) {
		super(message);
	}
	
	public SpreadsheetException(String message, Throwable cause) {
		super(message, cause);
	}

}
