package io.konig.datacatalog;

import io.konig.shacl.Shape;

public class ShapeRequest extends PageRequest {

	private Shape shape;
	
	public ShapeRequest(PageRequest base) {
		super(base);
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
}
