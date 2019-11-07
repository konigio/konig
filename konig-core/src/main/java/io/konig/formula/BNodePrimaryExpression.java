package io.konig.formula;

import java.util.List;

public class BNodePrimaryExpression extends BNodeExpression implements PrimaryExpression {

	public BNodePrimaryExpression(List<PredicateObjectList> constraints) {
		super(constraints);
	}

}
