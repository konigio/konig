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
import io.konig.formula.PathExpression;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;

public class SequencePathBuilder implements ShapeFormulaBuilder {
	
	private WorkbookLocation location;
	private PropertyConstraint constraint;
	private String propertyPathText;

	public SequencePathBuilder(WorkbookLocation location, PropertyConstraint constraint, String propertyPathText) {
		this.location = location;
		this.constraint = constraint;
		this.propertyPathText = propertyPathText;
	}

	@Override
	public void build(WorkbookProcessor processor, Shape shape, FormulaParser formulaParser) throws SpreadsheetException {

		try {
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
