package io.konig.transform.factory;

import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.JoinStatement;
import io.konig.transform.rule.VariableNamer;

public class ProtoJoinStatement {

	private SourceShape left;
	private SourceShape right;
	private BooleanExpression condition;
	
	public ProtoJoinStatement(SourceShape left, SourceShape right, BooleanExpression condition) {
		this.left = left;
		this.right = right;
		this.condition = condition;
	}

	public SourceShape getLeft() {
		return left;
	}

	public SourceShape getRight() {
		return right;
	}

	public BooleanExpression getCondition() {
		return condition;
	}
	
}
