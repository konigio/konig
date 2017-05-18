package io.konig.transform.rule;

public class JoinStatement {
	
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
	
	
}
