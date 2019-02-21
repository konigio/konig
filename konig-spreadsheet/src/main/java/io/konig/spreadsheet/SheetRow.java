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
