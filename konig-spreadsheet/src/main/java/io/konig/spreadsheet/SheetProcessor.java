package io.konig.spreadsheet;

public interface SheetProcessor extends Comparable<SheetProcessor> {
	
	/**
	 * Get a list of other SheetProcessors that must be executed before this one.
	 */
	SheetProcessor[] dependsOn();
	
	
	SheetColumn[] getColumns();
	
	SheetColumn findColumnByName(String name);
	
	void visit(SheetRow row) throws SpreadsheetException;
	
	/**
	 * Test whether this SheetProcessor depends on another processor, transitively.
	 * @param other The other SheetProcessor
	 * @return True if this sheet depends on the other sheet, or any sheet that the
	 * other sheet depends on, recursively.
	 */
	public boolean transitiveDependsOn(SheetProcessor other);
}
