package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class SimpleWhenClause extends AbstractExpression {
	
	private ValueExpression whenOperand;
	private Result result;
	
	

	public SimpleWhenClause(ValueExpression whenOperand, Result result) {
		this.whenOperand = whenOperand;
		this.result = result;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("WHEN ");
		whenOperand.print(out);
		out.print(" THEN ");
		result.print(out);
	}

}
