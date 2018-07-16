package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class TruthValue extends AbstractExpression implements BooleanTest {
	
	public static final TruthValue TRUE = new TruthValue("TRUE");
	public static final TruthValue FALSE = new TruthValue("FALSE");
	
	private String text;
	
	private TruthValue(String text) {
		this.text = text;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(text);
	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
	}

}
