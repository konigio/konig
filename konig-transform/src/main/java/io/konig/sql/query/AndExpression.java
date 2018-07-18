package io.konig.sql.query;

/*
 * #%L
 * Konig Transform
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

public class AndExpression extends AbstractExpression implements BooleanTerm {
	
	private List<BooleanTerm> termList = new ArrayList<>();

	public AndExpression() {
	}
	
	public AndExpression(BooleanTerm... term) {
		for (BooleanTerm t : term) {
			add(t);
		}
	}
	
	public void add(BooleanTerm term) {
		termList.add(term);
	}

	public List<BooleanTerm> getTermList() {
		return termList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		String and = null;
		out.pushIndent();
		for (BooleanTerm term : termList) {
			if (and != null) {
				out.println();
				out.indent();
				out.print(and);
			} 
			if (term instanceof OrExpression)  {
				out.print('(');
			}
			term.print(out);
			if (term instanceof OrExpression) {
				out.print(')');
			}
			and = "AND ";
		}
		out.popIndent();
		
		
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		for (BooleanTerm term : termList) {
			visit(visitor, "term", term);
		}
		
	}

}
