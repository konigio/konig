package io.konig.sql.query;

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
		caseOperand.print(out);
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

}
