package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
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


import java.util.ArrayList;
import java.util.List;

import io.konig.formula.FunctionExpression;

public class TFunctionExpression implements TExpression {
	
	private FunctionTPropertyShape propertyShape;
	private List<TExpression> argList = new ArrayList<>();

	public TFunctionExpression(FunctionTPropertyShape propertyShape) {
		this.propertyShape = propertyShape;
	}

	public FunctionTPropertyShape getPropertyShape() {
		return propertyShape;
	}
	
	public FunctionExpression getFunctionExpression() {
		return (FunctionExpression) propertyShape.getConstraint().getFormula().asPrimaryExpression();
	}

	@Override
	public TPropertyShape valueOf() {
		return propertyShape;
	}

	public List<TExpression> getArgList() {
		return argList;
	}
	
	public void addArg(TExpression arg) {
		argList.add(arg);
	}

}
