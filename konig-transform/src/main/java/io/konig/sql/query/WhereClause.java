package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class WhereClause extends AbstractExpression {
	private BooleanTerm condition;

	public WhereClause(BooleanTerm term) {
		this.condition = term;
	}

	public BooleanTerm getCondition() {
		return condition;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("WHERE ");
		out.print(condition);
	}

}
