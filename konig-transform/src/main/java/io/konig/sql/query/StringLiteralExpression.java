package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class StringLiteralExpression extends AbstractExpression {
	
	private String value;

	public StringLiteralExpression(String value) {
		this.value = value;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print('"');
		out.print(value);
		out.print('"');
	}

	public String getValue() {
		return value;
	}
	
}
