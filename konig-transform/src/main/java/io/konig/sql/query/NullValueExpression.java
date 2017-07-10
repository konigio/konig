package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class NullValueExpression extends AbstractExpression implements ValueExpression {

	public NullValueExpression() {
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("NULL");
	}

}
