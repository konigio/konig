package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class IfExpression extends AbstractExpression implements NumericValueExpression {
	
	private BooleanTerm condition;
	private ValueExpression whenTrue;
	private ValueExpression whenFalse;
	

	public IfExpression(BooleanTerm condition, ValueExpression whenTrue, ValueExpression whenFalse) {
		this.condition = condition;
		this.whenTrue = whenTrue;
		this.whenFalse = whenFalse;
	}
	
	public BooleanTerm getCondition() {
		return condition;
	}

	public ValueExpression getWhenTrue() {
		return whenTrue;
	}

	public ValueExpression getWhenFalse() {
		return whenFalse;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("IF(");
		out.print(condition);
		out.print(" , ");
		out.print(whenTrue);
		out.print(" , ");
		out.print(whenFalse);
		out.print(')');
	}

}
