package io.konig.spreadsheet;

public class WorkbookLocation {
	
	private String workbookName;
	private String sheetName;
	private Integer rowNum;
	private String columnName;

	public WorkbookLocation() {
	}

	public WorkbookLocation(String workbookName, String sheetName, Integer rowNum, String columnName) {
		this.workbookName = workbookName;
		this.sheetName = sheetName;
		this.rowNum = rowNum;
		this.columnName = columnName;
	}

	public WorkbookLocation(String workbookName, String sheetName, int rowNum) {
		this.workbookName = workbookName;
		this.sheetName = sheetName;
		this.rowNum = rowNum;
	}

	public WorkbookLocation(String workbookName, String sheetName) {
		this.workbookName = workbookName;
		this.sheetName = sheetName;
	}

	public String getWorkbookName() {
		return workbookName;
	}

	public String getSheetName() {
		return sheetName;
	}

	public Integer getRowNum() {
		return rowNum;
	}

	public String getColumnName() {
		return columnName;
	}
	
	

}
