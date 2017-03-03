package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class UpdateItem extends AbstractExpression {

	private PathExpression left;
	private QueryExpression right;
	
	public UpdateItem(PathExpression left, QueryExpression right) {
		this.left = left;
		this.right = right;
	}
	
	public PathExpression getLeft() {
		return left;
	}

	public QueryExpression getRight() {
		return right;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		left.print(out);
		out.print(" = ");
		right.print(out);

	}

}
