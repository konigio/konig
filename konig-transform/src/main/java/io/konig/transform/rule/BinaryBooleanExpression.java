package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class BinaryBooleanExpression extends AbstractPrettyPrintable implements BooleanExpression {

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
	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("operator", operator.toString());
		out.field("leftPredicate", leftPredicate);
		out.field("rightPredicate", rightPredicate);
		out.endObject();
		
	}
	
	
}
