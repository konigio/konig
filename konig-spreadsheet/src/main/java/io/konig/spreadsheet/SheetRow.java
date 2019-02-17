package io.konig.spreadsheet;

import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;

public class SheetRow {

	private WorkbookSheet sheet;
	private Row row;
	private List<SheetColumn> undeclaredColumns;
	

	public SheetRow(WorkbookSheet sheet, Row row) {
		this.sheet = sheet;
		this.row = row;
	}

	public Row getRow() {
		return row;
	}
	
	public int getRowNum() {
		return row.getRowNum();
	}
	
	public String getSheetName() {
		return row.getSheet().getSheetName();
	}

	public void setRow(Row row) {
		this.row = row;
	}

	public List<SheetColumn> getUndeclaredColumns() {
		return undeclaredColumns==null ? Collections.emptyList() : undeclaredColumns;
	}

	public void setUndeclaredColumns(List<SheetColumn> undeclaredColumns) {
		this.undeclaredColumns = undeclaredColumns;
	}

	public WorkbookSheet getSheet() {
		return sheet;
	}

	
	
	
}
