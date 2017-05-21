package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class JoinStatement extends AbstractPrettyPrintable {
	
	private DataChannel left;
	private DataChannel right;
	private BooleanExpression condition;
	
	public JoinStatement(DataChannel left, DataChannel right, BooleanExpression condition) {
		this.left = left;
		this.right = right;
		this.condition = condition;
	}

	public DataChannel getLeft() {
		return left;
	}

	public DataChannel getRight() {
		return right;
	}

	public BooleanExpression getCondition() {
		return condition;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		
		out.beginObjectField("left", left);
		out.field("name", left.getName());
		out.endObjectField(left);

		out.beginObjectField("right", right);
		out.field("name", right.getName());
		out.endObjectField(right);
		
		out.field("condition", condition);
		out.endObject();
		
	}
	
	
}
