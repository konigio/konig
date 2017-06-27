package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class CastSpecification extends AbstractExpression implements ValueExpression {

	private ValueExpression value;
	private String datatype;

	public CastSpecification(ValueExpression value, String datatype) {
		this.value = value;
		this.datatype = datatype;
	}

	public ValueExpression getValue() {
		return value;
	}

	public String getDatatype() {
		return datatype;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("CAST(");
		out.print(value);
		out.print(" AS ");
		out.print(datatype);
		out.print(')');
	}

}
