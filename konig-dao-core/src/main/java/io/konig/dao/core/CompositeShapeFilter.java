package io.konig.dao.core;

import java.util.ArrayList;
import java.util.Collection;

public class CompositeShapeFilter extends ArrayList<ShapeFilter> implements ShapeFilter {
	private static final long serialVersionUID = 1L;
	
	private CompositeOperator operator;

	public CompositeShapeFilter(CompositeOperator operator) {
		this.operator = operator;
	}

	public CompositeOperator getOperator() {
		return operator;
	}

}
