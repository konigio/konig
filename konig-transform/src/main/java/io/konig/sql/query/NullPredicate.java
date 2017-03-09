package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class NullPredicate extends AbstractExpression implements BooleanPrimary {
	
	private ValueExpression value;
	private boolean isNull;

	public NullPredicate(ValueExpression value, boolean isNull) {
		this.value = value;
		this.isNull = isNull;
	}

	public ValueExpression getValue() {
		return value;
	}

	public boolean isNull() {
		return isNull;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(value);
		out.print(" IS ");
		if (!isNull) {
			out.print("NOT ");
		}
		out.print("NULL");

	}

}
