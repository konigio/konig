package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class NotExpression extends AbstractExpression implements BooleanTest {
	private BooleanTerm term;
	

	public NotExpression(BooleanTerm term) {
		this.term = term;
	}

	public BooleanTerm getTerm() {
		return term;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("NOT ");
		out.print(term);

	}

}
