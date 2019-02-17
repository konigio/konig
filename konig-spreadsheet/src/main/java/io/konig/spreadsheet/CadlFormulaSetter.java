package io.konig.spreadsheet;

import java.io.IOException;

import org.openrdf.rio.RDFParseException;

import io.konig.cadl.HasFormula;
import io.konig.core.LocalNameService;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.rio.turtle.NamespaceMap;

public class CadlFormulaSetter implements Action {
	
	private LocalNameService localNameService;
	private NamespaceMap nsMap;
	private HasFormula container;
	private String formulaText;
	

	public CadlFormulaSetter(LocalNameService localNameService, NamespaceMap nsMap, HasFormula container,
			String formulaText) {
		this.localNameService = localNameService;
		this.nsMap = nsMap;
		this.container = container;
		this.formulaText = formulaText;
	}


	@Override
	public void execute() throws SpreadsheetException {
		FormulaParser parser = new FormulaParser(null, localNameService, nsMap);
		try {
			QuantifiedExpression e = parser.quantifiedExpression(formulaText);
			container.setFormula(e);
		} catch (RDFParseException | IOException e) {
			throw new SpreadsheetException(e);
		}

	}

}
