package io.konig.core.showl;

import java.util.Set;

public class ShowlEqualStatement implements ShowlStatement {
	
	private ShowlExpression left;
	private ShowlExpression right;
	
	public ShowlEqualStatement(ShowlExpression left, ShowlExpression right) {
		this.left = left;
		this.right = right;
	}

	public ShowlExpression getLeft() {
		return left;
	}

	public ShowlExpression getRight() {
		return right;
	}
	
	public String toString() {
		return left.displayValue() + " = " + right.displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNode, Set<ShowlPropertyShape> set) {
		left.addDeclaredProperties(sourceNode, set);
		right.addDeclaredProperties(sourceNode, set);
		
	}



}
