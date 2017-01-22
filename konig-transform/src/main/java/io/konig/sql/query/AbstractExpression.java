package io.konig.sql.query;

public abstract class AbstractExpression implements QueryExpression {

	public String toString() {
		StringBuilder builder = new StringBuilder();
		append(builder);
		return builder.toString();
	}

}
