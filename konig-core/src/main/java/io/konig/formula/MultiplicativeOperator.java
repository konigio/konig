package io.konig.formula;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import io.konig.core.io.PrettyPrintWriter;

public enum MultiplicativeOperator implements Formula {
	MULTIPLY('*'),
	DIVIDE('/')
	;
	
	private char symbol;
	private MultiplicativeOperator(char symbol) {
		this.symbol = symbol;
	}
	@Override
	public void print(PrettyPrintWriter out) {
		out.print(symbol);
	}
	public char getSymbol() {
		return symbol;
	}
	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		visitor.exit(this);
	}
	

	public PrimaryExpression asPrimaryExpression() {
		return null;
	}
	
	
	public BinaryRelationalExpression asBinaryRelationalExpression() {
		return null;
	}


}
