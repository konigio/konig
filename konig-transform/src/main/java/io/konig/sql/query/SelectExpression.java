package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class SelectExpression extends BaseValueContainer implements ValueContainer, QueryExpression {
	
	
	private FromExpression from = new FromExpression();
	
	public FromExpression getFrom() {
		return from;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("SELECT");
		out.pushIndent();
		String comma = "";
		for (ValueExpression value : getValues()) {
			out.print(comma);
			out.println();
			out.indent();
			value.print(out);
			comma = ",";
		}
		out.popIndent();
		out.println();
		if (from != null) {
			from.print(out);
		}
		
	}
	

}
