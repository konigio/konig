package io.konig.spreadsheet;

public class MissingColumnException extends SpreadsheetException {
	private static final long serialVersionUID = 1L;

	public MissingColumnException(String columnName, String sheetName) {
		super("'" + columnName + "' is missing from the '" + sheetName + "' sheet");
	}

}
