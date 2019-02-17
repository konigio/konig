package io.konig.spreadsheet;

import io.konig.spreadsheet.nextgen.Workbook;

public interface WorkbookListener {
	
	public void beginWorkbook(Workbook workbook);
	public void endWorkbook(Workbook workbook);

}
