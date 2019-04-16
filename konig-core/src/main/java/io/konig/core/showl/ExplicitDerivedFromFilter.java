package io.konig.core.showl;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;

/**
 * A filter that accepts only those shapes where the explicitDerivedFrom set is
 * non-empty.
 * 
 * @author Greg McFall
 *
 */
public class ExplicitDerivedFromFilter implements ShapeFilter {

	@Override
	public boolean accept(Shape shape) {
		
		return !shape.getExplicitDerivedFrom().isEmpty();
	}

}
