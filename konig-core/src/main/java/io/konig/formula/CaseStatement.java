package io.konig.formula;

import java.util.ArrayList;

/*
 * #%L
 * Konig Core
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


import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class CaseStatement extends AbstractFormula implements PrimaryExpression {
	
	private Expression caseCondition;
	private List<WhenThenClause> whenThenList;
	private Expression elseClause;

	public CaseStatement(Expression caseCondition, List<WhenThenClause> whenThenList, Expression elseClause) {
		this.caseCondition = caseCondition;
		this.whenThenList = whenThenList;
		this.elseClause = elseClause;
	}
	
	@Override
	public CaseStatement clone() {
		Expression otherCaseCondition = caseCondition == null ? null : caseCondition.clone();
		List<WhenThenClause> otherWhenThenList = whenThenList==null ? null : clone(whenThenList);
		Expression otherElseClause = elseClause == null ? null : elseClause.clone();
		
		return new CaseStatement(otherCaseCondition, otherWhenThenList, otherElseClause);
	}

	private List<WhenThenClause> clone(List<WhenThenClause> list) {
		List<WhenThenClause> result = new ArrayList<>();
		for (WhenThenClause c : list) {
			result.add(c.clone());
		}
		return result;
	}

	public Expression getCaseCondition() {
		return caseCondition;
	}

	public List<WhenThenClause> getWhenThenList() {
		return whenThenList;
	}

	public Expression getElseClause() {
		return elseClause;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.setSuppressContext(true);
		out.println();
		out.pushIndent();
		out.indent();
		out.print("CASE");
		if (caseCondition != null) {
			out.print(' ');
			caseCondition.print(out);
		}
		out.pushIndent();
		for (WhenThenClause clause : whenThenList) {
			clause.print(out);
		}
		if (elseClause != null) {
			out.println();
			out.indent();
			out.print("ELSE ");
			elseClause.print(out);
		}
		
		out.popIndent();
		out.println();
		out.indent();
		out.print("END");
		out.popIndent();

	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (WhenThenClause whenThen : whenThenList) {
			whenThen.dispatch(visitor);
		}
		if (elseClause != null) {
			elseClause.dispatch(visitor);
		}
		visitor.exit(this);

	}

}
