package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class ComparisonPredicate extends AbstractExpression implements BooleanTerm {

	private ComparisonOperator operator;
	private ColumnExpression left;
	private ColumnExpression right;
	
	public ComparisonPredicate(ComparisonOperator operator, ColumnExpression left, ColumnExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}


	public ComparisonOperator getOperator() {
		return operator;
	}


	public ColumnExpression getLeft() {
		return left;
	}



	public ColumnExpression getRight() {
		return right;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		left.print(out);
		out.print(operator.getText());
		right.print(out);
	}

}
