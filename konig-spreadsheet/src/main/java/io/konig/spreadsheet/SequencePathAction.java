package io.konig.spreadsheet;

import org.openrdf.model.URI;

import io.konig.formula.FormulaParser;
import io.konig.formula.PathExpression;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.SequencePath;

public class SequencePathAction implements Action {
	
	private WorkbookProcessor processor;
	private WorkbookLocation location;
	private URI shapeId;
	private PropertyConstraint constraint;
	private String propertyPathText;
	private FormulaParserFactory formulaParserFactory;
	
	public SequencePathAction(WorkbookProcessor processor, WorkbookLocation location, URI shapeId, PropertyConstraint constraint,
			String propertyPathText, FormulaParserFactory formulaParserFactory) {
		this.processor = processor;
		this.location = location;
		this.shapeId = shapeId;
		this.constraint = constraint;
		this.propertyPathText = propertyPathText;
		this.formulaParserFactory = formulaParserFactory;
	}



	@Override
	public void execute() throws SpreadsheetException {
		
		try {
			FormulaParser formulaParser = formulaParserFactory.forShape(shapeId);
			QuantifiedExpression formula = formulaParser.quantifiedExpression(propertyPathText);
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof PathExpression) {
				 PathExpression pathExpression = (PathExpression) primary;
				 try {
					 SequencePath sequence = SequencePath.fromPathExpression(pathExpression);
					 constraint.setPath(sequence);
				 } catch (Throwable e) {
					 processor.fail(location, e.getMessage());
				 }
				 
			} else {
				processor.fail(location, "The given expression is not a PropertyPath: {0}", 
					propertyPathText);
			}
		} catch (Throwable e) {
			processor.fail(e, location, "Failed to parse PropertyPath: {0}", propertyPathText);
		}

	}

}
