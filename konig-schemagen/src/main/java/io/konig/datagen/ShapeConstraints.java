package io.konig.datagen;

import org.openrdf.model.URI;

import io.konig.annotation.OwlClass;

@OwlClass("http://www.konig.io/ns/core/SyntheticShapeConstraints")
public class ShapeConstraints {

	private URI targetShape;
	private Integer shapeCount;
	
	public URI getConstrainedShape() {
		return targetShape;
	}

	public void setConstrainedShape(URI targetShape) {
		this.targetShape = targetShape;
	}

	public Integer getShapeInstanceCount() {
		return shapeCount;
	}

	public void setShapeInstanceCount(Integer shapeCount) {
		this.shapeCount = shapeCount;
	}
	
	
}
