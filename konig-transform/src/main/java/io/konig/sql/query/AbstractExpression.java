package io.konig.sql.query;

import java.io.StringWriter;

import io.konig.core.io.PrettyPrintWriter;

public abstract class AbstractExpression implements QueryExpression {

	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		print(out);
		out.close();
		return buffer.toString();
	}

}
