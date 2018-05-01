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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class ConditionalAndExpression extends AbstractFormula {
	private List<ValueLogical> andList = new ArrayList<>();
	
	public void add(ValueLogical expr)  {
		andList.add(expr);
	}

	public List<ValueLogical> getAndList() {
		return andList;
	}

	@Override
	public void print(PrettyPrintWriter out) {

		String operator = "";
		for (ValueLogical e : andList) {
			out.print(operator);
			e.print(out);
			operator = " && ";
		}
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (ValueLogical logical : andList) {
			logical.dispatch(visitor);
		}
		visitor.exit(this);
		
	}

	@Override
	public ConditionalAndExpression deepClone() {
		ConditionalAndExpression clone = new ConditionalAndExpression();
		for (ValueLogical value : andList) {
			clone.add((ValueLogical)value);
		}
		return clone;
	}

}
