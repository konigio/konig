package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class AliasExpression extends AbstractExpression 
implements ValueExpression, GroupingElement {
	private QueryExpression expression;
	private String alias;

	
	public AliasExpression(QueryExpression expression, String alias) {
		this.expression = expression;
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}


	public QueryExpression getExpression() {
		return expression;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		expression.print(out);
		out.append(" AS ");
		out.append(alias);
	}


}
