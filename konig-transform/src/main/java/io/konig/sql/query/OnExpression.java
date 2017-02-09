package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class OnExpression extends AbstractExpression implements SearchCondition {
	
	private SearchCondition searchCondition;

	public OnExpression(SearchCondition searchCondition) {
		this.searchCondition = searchCondition;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println(" ON");
		out.pushIndent();
		out.indent();
		searchCondition.print(out);
		out.popIndent();
	}

}
