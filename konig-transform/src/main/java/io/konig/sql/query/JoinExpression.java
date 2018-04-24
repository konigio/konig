package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class JoinExpression extends AbstractExpression implements TableItemExpression {
	
	private TableReference table;
	private OnExpression joinSpecification;

	public JoinExpression(TableReference table, OnExpression joinSpecification) {
		this.table = table;
		this.joinSpecification = joinSpecification;
	}

	public TableReference getTable() {
		return table;
	}

	public OnExpression getJoinSpecification() {
		return joinSpecification;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println();
		out.indent();
		out.print("JOIN ");
		table.print(out);
		joinSpecification.print(out);

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "table", table);
		visit(visitor, "joinSpecification", joinSpecification);
	}

}
