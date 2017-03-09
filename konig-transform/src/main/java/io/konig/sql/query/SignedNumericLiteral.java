package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class SignedNumericLiteral extends AbstractExpression implements Factor {
	
	private Number number;
	
	public SignedNumericLiteral(Number number) {
		this.number = number;
	}

	public Number getNumber() {
		return number;
	}

	public void setNumber(Number number) {
		this.number = number;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(number);
	}

}
