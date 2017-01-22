package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

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
	public void append(StringBuilder builder) {
		
		builder.append(functionName);
		builder.append('(');
		String comma = "";
		for (QueryExpression e : argList) {
			builder.append(comma);
			e.append(builder);
			comma = ", ";
		}
		
		builder.append(')');
		
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<QueryExpression> getArgList() {
		return argList;
	}

}
