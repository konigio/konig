package io.konig.spreadsheet;

public interface ActionListener {

	/**
	 * Receive notification immediately before deferred actions are invoked.
	 */
	void beforeActions() throws SpreadsheetException;
	
	/**
	 * Receive notification immediately after deferred actions are invoked.
	 */
	void afterActions() throws SpreadsheetException;
}
