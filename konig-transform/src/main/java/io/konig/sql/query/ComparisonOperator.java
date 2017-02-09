package io.konig.sql.query;

public enum ComparisonOperator {
	EQUALS("=") ;

	private String text;
	private ComparisonOperator(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

}
