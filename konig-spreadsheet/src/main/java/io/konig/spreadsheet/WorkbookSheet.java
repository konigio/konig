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


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class WorkbookSheet implements Comparable<WorkbookSheet>{
	
	
	private Sheet sheet;
	private SheetProcessor processor;
	private SheetRow headerRow;
	
	public WorkbookSheet(Sheet sheet, SheetProcessor processor) {
		this.sheet = sheet;
		this.processor = processor;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public SheetProcessor getProcessor() {
		return processor;
	}

	@Override
	public int compareTo(WorkbookSheet o) {
		return processor.compareTo(o.getProcessor());
	}

	public String toString() {
		return "WorkbookSheet(" + sheet.getSheetName() + ")";
	}
	
	public SheetRow getHeaderRow() {
		if (headerRow == null) {
			int rowNum = sheet.getFirstRowNum();
			Row row = sheet.getRow(rowNum);
			headerRow = new SheetRow(this, row);
		}
		return headerRow;
	}

	
}
