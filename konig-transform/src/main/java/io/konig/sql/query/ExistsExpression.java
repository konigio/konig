package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class ExistsExpression extends AbstractExpression implements BooleanTest {
	private SelectExpression selectQuery;
	

	public ExistsExpression(SelectExpression selectQuery) {
		this.selectQuery = selectQuery;
	}

	public SelectExpression getSelectQuery() {
		return selectQuery;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println("EXISTS(");
		out.pushIndent();
		out.indent();
		out.print(selectQuery);
		out.popIndent();
		out.println(')');

	}

}
