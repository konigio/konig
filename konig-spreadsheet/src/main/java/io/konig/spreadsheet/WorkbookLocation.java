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
