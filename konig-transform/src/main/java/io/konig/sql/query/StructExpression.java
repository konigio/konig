package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class StructExpression extends BaseValueContainer
implements ItemExpression, ValueContainer {
	
	
	@Override
	public void print(PrettyPrintWriter out) {
		boolean prettyPrint = out.isPrettyPrint();
		
		out.print("STRUCT(");
		out.pushIndent();
		String comma = "";
		for (QueryExpression field : getValues()) {
			out.print(comma);
			if (prettyPrint || comma.length()>0) {
				out.println();
			}
			out.indent();
			field.print(out);
			comma = ",";
		}
		if (prettyPrint) {
			out.print('\n');
		}
		out.popIndent();
		out.indent();
		out.print(')');
	}

}
