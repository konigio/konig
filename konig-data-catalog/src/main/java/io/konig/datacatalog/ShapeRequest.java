package io.konig.datacatalog;

import java.io.File;

import io.konig.shacl.Shape;

public class ShapeRequest extends PageRequest {

	private Shape shape;
	private File exampleDir;
	
	public ShapeRequest(PageRequest base, Shape shape, File exampleDir) {
		super(base);
		this.shape = shape;
		this.exampleDir = exampleDir;
	}

	public Shape getShape() {
		return shape;
	}

	public File getExamplesDir() {
		return exampleDir;
	}
	
}
