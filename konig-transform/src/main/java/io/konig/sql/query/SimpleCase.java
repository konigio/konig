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


import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class SimpleCase extends AbstractExpression implements CaseSpecification {
	private ValueExpression caseOperand;
	private List<SimpleWhenClause> whenClauseList;
	private Result elseClause;

	public SimpleCase(ValueExpression caseOperand, List<SimpleWhenClause> whenClauseList, Result elseClause) {
		this.caseOperand = caseOperand;
		this.whenClauseList = whenClauseList;
		this.elseClause = elseClause;
	}
	
	public ValueExpression getCaseOperand() {
		return caseOperand;
	}

	public List<SimpleWhenClause> getWhenClauseList() {
		return whenClauseList;
	}

	public Result getElseClause() {
		return elseClause;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("CASE ");
		if (caseOperand != null) {
			caseOperand.print(out);
		}
		out.println();
		out.pushIndent();
		for (SimpleWhenClause when : whenClauseList) {
			out.indent();
			when.print(out);
			out.println();
		}
		out.popIndent();
		if (elseClause != null) {
			out.indent();
			elseClause.print(out);
			out.println();
		}
		out.indent();
		out.print("END");

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "caseOperand", caseOperand);
		visit(visitor, "elseClause", elseClause);
		if (whenClauseList != null) {
			for (SimpleWhenClause clause : whenClauseList) {
				visit(visitor, "whenClause", clause);
			}
		}
		
	}

}
