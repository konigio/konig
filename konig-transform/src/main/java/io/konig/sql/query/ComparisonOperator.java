package io.konig.sql.query;

public enum ComparisonOperator {
	EQUALS("="),
	NOT_EQUALS("<>"),
	LESS_THAN("<"),
	GREATER_THAN(">"),
	LESS_THAN_OR_EQUALS("<="),
	GREATER_THAN_OR_EQUALS(">=");

	private String text;
	private ComparisonOperator(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

}
