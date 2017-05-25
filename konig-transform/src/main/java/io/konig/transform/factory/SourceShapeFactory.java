package io.konig.transform.factory;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

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

	@Override
	protected SourceProperty property(PropertyConstraint p, int pathIndex, URI predicate, Value value) {
		return new SourceProperty(p, pathIndex, predicate, value);
	}
	public SourceShape createShapeNode(Shape shape) {
		SourceShape result = super.createShapeNode(shape);
		List<PropertyConstraint> list = shape.getDerivedProperty();
		if (list != null) {
			this.addProperties(result, list, true);
		}
		return result;
	}

}
