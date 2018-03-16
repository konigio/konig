package io.konig.formula;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


/**
 * A Formula visitor that determines whether some formula contains a FunctionExpression 
 * that performs aggregation.
 * 
 * @author Greg McFall
 *
 */
public class HasAggregationVisitor implements FormulaVisitor {
	private boolean visitedAggregation=false;

	public HasAggregationVisitor() {
		
	}
	
	public void reset() {
		visitedAggregation = false;
	}
	
	/**
	 * Returns true if this visitor encountered a FunctionExpression that performs an aggregation 
	 * and false otherwise.
	 */
	public boolean visitedAggregation() {
		return visitedAggregation;
	}

	@Override
	public void enter(Formula formula) {
		
		if (formula instanceof FunctionExpression) {
			FunctionExpression func = (FunctionExpression) formula;
			if (func.isAggregation()) {
				visitedAggregation = true;
			}
		}
	}

	@Override
	public void exit(Formula formula) {
		// Do nothing
	}

}
