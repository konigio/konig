package io.konig.spreadsheet;

import io.konig.formula.FormulaParser;
import io.konig.shacl.Shape;

public interface ShapeFormulaBuilder {

	void build(WorkbookProcessor processor, Shape shape, FormulaParser parser)
	throws SpreadsheetException;
}
