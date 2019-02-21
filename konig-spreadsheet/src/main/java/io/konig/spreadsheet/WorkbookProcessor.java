package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.shacl.ShapeManager;
import io.konig.spreadsheet.nextgen.Workbook;

public interface WorkbookProcessor {
	
	ServiceManager getServiceManager();
	
	String stringValue(SheetRow row, SheetColumn col);
	
	Graph getGraph();
	
	OwlReasoner getOwlReasoner();
	
	URI iriValue(SheetRow row, SheetColumn col) throws SpreadsheetException;
	
	Workbook getActiveWorkbook();
	
	void addSheetProcessor(SheetProcessor processor);
	
	void processAll(List<File> files) throws SpreadsheetException;
	
	void process(File workbookFile) throws SpreadsheetException;
	

	void defer(Action action);
	
	void executeDeferredActions() throws SpreadsheetException;
	
	void fail(Throwable cause, SheetRow row, SheetColumn column, String pattern, Object...arg) throws SpreadsheetException;

	void fail(WorkbookLocation location, String pattern, Object...arg) throws SpreadsheetException;
	void fail(Throwable cause, WorkbookLocation location, String pattern, Object...arg) throws SpreadsheetException;

	URI uri(String stringValue);

	WorkbookLocation location(SheetRow sheetRow, SheetColumn column);
	
	URI expandCurie(String text, SheetRow row, SheetColumn col) throws SpreadsheetException;
	URI expandCurie(String text, WorkbookLocation location) throws SpreadsheetException;
	
	String getSetting(String name);

	ShapeManager getShapeManager();

	void warn(WorkbookLocation location, String pattern, Object...args);
	
	<T> T service(Class<T> javaClass);

}
