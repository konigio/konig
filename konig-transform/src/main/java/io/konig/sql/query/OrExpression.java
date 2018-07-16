package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class OrExpression extends AbstractExpression implements BooleanTerm {
	
	private AndExpression left;
	private AndExpression right;
	

	public OrExpression(AndExpression left, AndExpression right) {
		this.left = left;
		this.right = right;
	}
	
	

	public AndExpression getLeft() {
		return left;
	}



	public AndExpression getRight() {
		return right;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		out.print(left);
		out.print(" OR ");
		out.print(right);

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visitor.visit(this, "left", left);
		visitor.visit(this, "right", right);
	}

}
