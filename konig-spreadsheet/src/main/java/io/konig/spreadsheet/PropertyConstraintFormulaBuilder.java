package io.konig.spreadsheet;

import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class PropertyConstraintFormulaBuilder implements ShapeFormulaBuilder {
	private WorkbookLocation location;
	private PropertyConstraint constraint;
	private String formulaText;
	

	public PropertyConstraintFormulaBuilder(WorkbookLocation location, PropertyConstraint constraint,
			String formulaText) {
		this.location = location;
		this.constraint = constraint;
		this.formulaText = formulaText;
	}


	@Override
	public void build(WorkbookProcessor processor, Shape shape, FormulaParser parser) throws SpreadsheetException {
		
		QuantifiedExpression formula;
		try {
			formula = parser.quantifiedExpression(formulaText);
			constraint.setFormula(formula);
		} catch (Throwable e) {
			processor.fail(e, location, "Failed to parse formula: {0}", formulaText);
		}
	}

}
