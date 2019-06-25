package io.konig.core.showl;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.formula.BinaryOperator;

public class ShowlBinaryRelationalExpression implements ShowlExpression {

	private BinaryOperator operator;
	private ShowlExpression left;
	private ShowlExpression right;
	
	public ShowlBinaryRelationalExpression(BinaryOperator operator, ShowlExpression left, ShowlExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public BinaryOperator getOperator() {
		return operator;
	}

	public ShowlExpression getLeft() {
		return left;
	}

	public ShowlExpression getRight() {
		return right;
	}

	@Override
	public String displayValue() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(left.displayValue());
		builder.append(' ');
		builder.append(operator.getText());
		builder.append(' ');
		builder.append(right.displayValue());
		
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		left.addDeclaredProperties(sourceNodeShape, set);
		right.addDeclaredProperties(sourceNodeShape, set);
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		left.addProperties(set);
		right.addProperties(set);
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return XMLSchema.BOOLEAN;
	}
	
	

}
