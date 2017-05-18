package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SourceShapeFactory extends ShapeNodeFactory<SourceShape, SourceProperty> {
	
	public static final SourceShapeFactory INSTANCE = new SourceShapeFactory();

	@Override
	protected SourceShape shape(Shape shape) {
		return new SourceShape(shape);
	}

	@Override
	protected SourceProperty property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch) {
		return new SourceProperty(p, pathIndex);
	}

}
