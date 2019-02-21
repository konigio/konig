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
