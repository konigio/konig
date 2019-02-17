package io.konig.spreadsheet;

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
