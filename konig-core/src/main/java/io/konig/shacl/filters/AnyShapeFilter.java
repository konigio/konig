package io.konig.shacl.filters;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;

/**
 * A filter that accepts all shapes
 * @author Greg McFall
 *
 */
public class AnyShapeFilter implements ShapeFilter {
	
	public static final AnyShapeFilter INSTANCE = new AnyShapeFilter();

	@Override
	public boolean accept(Shape shape) {
		return true;
	}

}
