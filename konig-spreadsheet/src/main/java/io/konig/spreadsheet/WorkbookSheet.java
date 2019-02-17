package io.konig.spreadsheet;

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
