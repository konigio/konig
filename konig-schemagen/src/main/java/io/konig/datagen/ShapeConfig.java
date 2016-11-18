package io.konig.datagen;

import org.openrdf.model.URI;

import io.konig.annotation.OwlClass;

@OwlClass("http://www.konig.io/ns/datagen/ShapeConfig")
public class ShapeConfig {

	private URI targetShape;
	private Integer shapeCount;
	
	public URI getTargetShape() {
		return targetShape;
	}

	public void setTargetShape(URI targetShape) {
		this.targetShape = targetShape;
	}

	public Integer getShapeCount() {
		return shapeCount;
	}

	public void setShapeCount(Integer shapeCount) {
		this.shapeCount = shapeCount;
	}
	
	
}
