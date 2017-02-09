package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class FunctionExpression extends AbstractExpression {
	private String functionName;
	private List<QueryExpression> argList = new ArrayList<>();
	public FunctionExpression(String functionName) {
		this.functionName = functionName;
	}
	
	public void addArg(QueryExpression arg) {
		argList.add(arg);
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print(functionName);
		out.print('(');
		String comma = "";
		for (QueryExpression e : argList) {
			out.print(comma);
			e.print(out);
			comma = ", ";
		}
		
		out.print(')');
		
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<QueryExpression> getArgList() {
		return argList;
	}

}
