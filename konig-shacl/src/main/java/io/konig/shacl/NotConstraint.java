package io.konig.shacl;

import io.konig.core.Vertex;

public class NotConstraint implements Constraint {

	private Shape shape;

	public NotConstraint(Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

	@Override
	public boolean accept(Vertex v) {
		return !GraphFilter.INSTANCE.matches(v, shape);
	}
	
	

}
