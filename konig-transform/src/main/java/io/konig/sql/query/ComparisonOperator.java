package io.konig.sql.query;

public enum ComparisonOperator {
	EQUALS("="),
	NOT_EQUALS("<>");

	private String text;
	private ComparisonOperator(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

}
