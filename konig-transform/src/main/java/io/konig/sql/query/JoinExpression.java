package io.konig.sql.query;

import io.konig.core.io.PrettyPrintWriter;

public class JoinExpression extends AbstractExpression implements TableItemExpression {
	
	private TableItemExpression leftTable;
	private TableItemExpression rightTable;
	private OnExpression joinSpecification;

	public JoinExpression(TableItemExpression leftTable, TableItemExpression rightTable,
			OnExpression joinSpecification) {
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.joinSpecification = joinSpecification;
	}
	
	public TableItemExpression getLeftTable() {
		return leftTable;
	}

	public TableItemExpression getRightTable() {
		return rightTable;
	}

	public OnExpression getJoinSpecification() {
		return joinSpecification;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.println();
		out.pushIndent();
		out.indent();
		leftTable.print(out);
		out.println();
		out.popIndent();
		out.indent();
		out.println(" JOIN");

		if (rightTable instanceof JoinExpression) {
			rightTable.print(out);
		} else {
			out.pushIndent();
			out.indent();
			rightTable.print(out);
			out.println();
			out.popIndent();
		}
		if (joinSpecification!= null) {
			joinSpecification.print(out);
		}
		
	}


}
