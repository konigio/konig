package io.konig.datacatalog;

import java.io.File;

import io.konig.shacl.Shape;

public class ShapeRequest extends PageRequest {

	private Shape shape;
	private File exampleDir;
	
	public ShapeRequest(PageRequest base, File exampleDir) {
		super(base);
		this.exampleDir = exampleDir;
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public File getExamplesDir() {
		return exampleDir;
	}
	
}
