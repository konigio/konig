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


import org.apache.poi.ss.usermodel.Cell;

public class CellValue {
	private Boolean booleanValue;
	private Double doubleValue;
	private String stringValue;
	
	public static enum Type {
		BOOLEAN,
		DOUBLE,
		STRING
	}
	
	public static CellValue getValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		
		switch (cell.getCellTypeEnum()) {
		case BOOLEAN : 
			Boolean booleanValue = cell.getBooleanCellValue();
			return new CellValue(booleanValue, null, booleanValue.toString());
			
		case NUMERIC :
			Double doubleValue = cell.getNumericCellValue();
			return new CellValue(null, doubleValue, doubleValue.toString());
			
		case STRING :
			String stringValue = cell.getStringCellValue();
			return new CellValue(null, null, stringValue);
			
		default:
			return null;
		}
	}
	
	private CellValue(Boolean booleanValue, Double doubleValue, String stringValue) {
		this.booleanValue = booleanValue;
		this.doubleValue = doubleValue;
		this.stringValue = stringValue;
	}
	
	public Type getType() {
		return 
			booleanValue!= null ? Type.BOOLEAN :
			doubleValue != null ? Type.DOUBLE :
			Type.STRING;
	}
	

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	

}
