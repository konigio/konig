package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class SelectTableItemExpression extends AbstractExpression implements TableItemExpression {
	private SelectExpression select;
	
	

	public SelectTableItemExpression(SelectExpression select) {
		this.select = select;
	}
	
	

	public SelectExpression getSelect() {
		return select;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		out.print('(');
		out.pushIndent();
		select.print(out);
		out.popIndent();
		out.indent();
		out.print(')');

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visitor.visit(this, "select", select);
	}

}
