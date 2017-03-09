package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.formula.AdditiveOperator;

public class AdditiveValueExpression extends AbstractExpression implements NumericValueExpression {
	private AdditiveOperator operator;
	private ValueExpression left;
	private ValueExpression right;

	public AdditiveValueExpression(AdditiveOperator operator, ValueExpression left,
			ValueExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}
	
	public AdditiveOperator getOperator() {
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
		out.print(left);
		out.print(' ');
		out.print(operator);
		out.print(' ');
		out.print(right);
	}

}
