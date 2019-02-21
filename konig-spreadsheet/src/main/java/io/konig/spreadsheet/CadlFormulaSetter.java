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
