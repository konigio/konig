package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class LinkedPathExpression extends AbstractExpression implements PathExpression {
	
	private PathExpression first;
	private LinkedPathExpression rest;

	public LinkedPathExpression(PathExpression first, LinkedPathExpression rest) {
		this.first = first;
		this.rest = rest;
	}
	
	public PathExpression getFirst() {
		return first;
	}

	public LinkedPathExpression getRest() {
		return rest;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		first.print(out);
		if (rest != null) {
			out.print('.');
			rest.print(out);
		}

	}

}
