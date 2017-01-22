package io.konig.sql.query;

public class StringLiteralExpression extends AbstractExpression {
	
	private String value;

	public StringLiteralExpression(String value) {
		this.value = value;
	}

	@Override
	public void append(StringBuilder builder) {
		builder.append('"');
		builder.append(value);
		builder.append('"');
	}

	public String getValue() {
		return value;
	}
	
}
