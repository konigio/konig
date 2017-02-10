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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.rio.RDFParseException;

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;

public class Expression extends AbstractFormula {

	private List<ConditionalAndExpression> orList;
	
	public Expression(String text) {
		FormulaParser parser = new FormulaParser();
		try {
			Expression self = parser.parse(text);
			orList = self.getOrList();
		} catch (RDFParseException | IOException e) {
			throw new KonigException(e);
		}
	}
	
	public Expression() {
		 orList = new ArrayList<>();
	}
	
	public void add(ConditionalAndExpression expr) {
		orList.add(expr);
	}

	public List<ConditionalAndExpression> getOrList() {
		return orList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		String operator = "";
		for (ConditionalAndExpression e : orList) {
			out.print(operator);
			e.print(out);
			operator = " || ";
		}
	}
	
	public Value toValue() {
		String text = toString();
		return new LiteralImpl(text);
	}
}
