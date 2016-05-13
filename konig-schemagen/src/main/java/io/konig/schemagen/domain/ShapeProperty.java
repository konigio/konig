package io.konig.schemagen.domain;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShapeProperty {
	private Shape shape;
	private PropertyConstraint constraint;
	public ShapeProperty(Shape shape, PropertyConstraint constraint) {
		this.shape = shape;
		this.constraint = constraint;
	}
	public Shape getShape() {
		return shape;
	}
	public PropertyConstraint getConstraint() {
		return constraint;
	}
	
	public boolean equals(Object other) {
		if (other instanceof ShapeProperty) {
			ShapeProperty s = (ShapeProperty) other;
			return s.shape==shape && s.constraint==constraint;
		}
		return false;
	}

}
