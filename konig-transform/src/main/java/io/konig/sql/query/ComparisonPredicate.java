package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class ComparisonPredicate extends AbstractExpression implements BooleanTerm {

	private ComparisonOperator operator;
	private ValueExpression left;
	private ValueExpression right;
	
	public ComparisonPredicate(ComparisonOperator operator, ValueExpression left, ValueExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}


	public ComparisonOperator getOperator() {
		return operator;
	}


	public ValueExpression getLeft() {
		return left;
	}



	public ValueExpression getRight() {
		return right;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		left.print(out);
		out.print(operator.getText());
		right.print(out);
	}

}
