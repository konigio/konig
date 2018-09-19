package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


public class SpreadsheetException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private Integer row;
	private String sheetName;
	
	public SpreadsheetException(Throwable cause) {
		super(cause);
	}
	
	public SpreadsheetException(String message) {
		super(message);
	}
	
	public SpreadsheetException(String message, Throwable cause) {
		super(message, cause);
	}

	public Integer getRow() {
		return row;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	
	public String getMessage() {
		String msg = super.getMessage();
		if (sheetName != null && row != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("On sheet '");
			builder.append(sheetName);
			builder.append("', row #");
			builder.append(row+1);
			builder.append("... ");
			builder.append(msg);
			msg = builder.toString();
		}
		
		return msg;
	}
	
	

}
