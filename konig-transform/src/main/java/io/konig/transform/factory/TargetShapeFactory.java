package io.konig.transform.factory;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class TargetShapeFactory extends ShapeNodeFactory<TargetShape, TargetProperty>  {
	
	
	public static final TargetShapeFactory INSTANCE = new TargetShapeFactory();
	
	public TargetShapeFactory() {
	}

	@Override
	protected TargetShape shape(Shape shape) {
		return new TargetShape(shape);
	}

	@Override
	protected TargetProperty property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch) {
		if (pathIndex < 0) {
			if (p.getEquivalentPath()==null) {
				return new BasicDirectTargetProperty(p);
			} else {
				return new AliasDirectTargetProperty(p, preferredMatch);
			}
		} else {
			if (preferredMatch==null) {
				return new ContainerIndirectTargetProperty(p, pathIndex);
			} else {
				return new LeafIndirectTargetProperty(p, pathIndex, preferredMatch);
			}
		}
	}

	@Override
	protected TargetProperty property(PropertyConstraint p, int pathIndex, URI predicate, Value value) {
		throw new KonigException("Value constraint not supported for property " + p.getPredicate());
	}

}
