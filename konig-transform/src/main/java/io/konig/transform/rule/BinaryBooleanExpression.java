package io.konig.transform.rule;

import org.openrdf.model.URI;

public class BinaryBooleanExpression implements BooleanExpression {

	private BooleanOperator operator;
	private URI leftPredicate;
	private URI rightPredicate;
	
	public BinaryBooleanExpression(BooleanOperator operator, URI leftPredicate, URI rightPredicate) {
		this.operator = operator;
		this.leftPredicate = leftPredicate;
		this.rightPredicate = rightPredicate;
	}
	public BooleanOperator getOperator() {
		return operator;
	}
	public URI getLeftPredicate() {
		return leftPredicate;
	}
	public URI getRightPredicate() {
		return rightPredicate;
	}
	
	
}
