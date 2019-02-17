package io.konig.spreadsheet;

import java.io.File;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.shacl.ShapeManager;

public interface WorkbookProcessor {
	
	ServiceManager getServiceManager();
	
	String stringValue(SheetRow row, SheetColumn col);
	
	Graph getGraph();
	
	OwlReasoner getOwlReasoner();
	
	URI iriValue(SheetRow row, SheetColumn col) throws SpreadsheetException;
	
	void addSheetProcessor(SheetProcessor processor);
	
	void process(File workbookFile) throws SpreadsheetException;
	

	void defer(Action action);
	
	void executeDeferredActions() throws SpreadsheetException;
	
	void fail(SheetRow row, SheetColumn column, Throwable cause, String pattern, Object...arg) throws SpreadsheetException;

	void fail(WorkbookLocation location, String pattern, Object...arg) throws SpreadsheetException;
	void fail(Throwable cause, WorkbookLocation location, String pattern, Object...arg) throws SpreadsheetException;

	URI uri(String stringValue);

	WorkbookLocation location(SheetRow sheetRow, SheetColumn column);
	
	URI expandCurie(String text, SheetRow row, SheetColumn col) throws SpreadsheetException;
	URI expandCurie(String text, WorkbookLocation location) throws SpreadsheetException;
	
	String getSetting(String name);

	ShapeManager getShapeManager();

	void warn(WorkbookLocation location, String pattern, Object...args);

}
