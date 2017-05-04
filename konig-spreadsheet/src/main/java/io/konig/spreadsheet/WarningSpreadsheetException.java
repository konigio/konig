package io.konig.spreadsheet;

import java.util.List;

public class WarningSpreadsheetException extends SpreadsheetException {
	private static final long serialVersionUID = 1L;

	public WarningSpreadsheetException(List<String> warningList) {
		super(stringify(warningList));
	}

	private static String stringify(List<String> warningList) {
		StringBuilder builder = new StringBuilder();
		for (String text : warningList) {
			builder.append(text);
			builder.append("\n");
		}
		return builder.toString();
	}
}
