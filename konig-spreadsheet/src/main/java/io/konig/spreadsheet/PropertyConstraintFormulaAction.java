package io.konig.spreadsheet;

import org.openrdf.model.URI;

import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;

public class PropertyConstraintFormulaAction implements Action {
	private WorkbookProcessor processor;
	private WorkbookLocation location;
	private URI shapeId;
	private PropertyConstraint constraint;
	private String formulaText;
	private FormulaParserFactory factory;
	
	public PropertyConstraintFormulaAction(WorkbookProcessor processor, WorkbookLocation location, URI shapeId,
			PropertyConstraint constraint, String formulaText, FormulaParserFactory factory) {
		this.processor = processor;
		this.location = location;
		this.shapeId = shapeId;
		this.constraint = constraint;
		this.formulaText = formulaText;
		this.factory = factory;
	}



	@Override
	public void execute() throws SpreadsheetException {
		
		try {
			FormulaParser parser = factory.forShape(shapeId);
			QuantifiedExpression formula = parser.quantifiedExpression(formulaText);
			constraint.setFormula(formula);
		} catch (Throwable e) {
			processor.fail(e, location, "Failed to parse formula: {0}", formulaText);
		}

	}

}
