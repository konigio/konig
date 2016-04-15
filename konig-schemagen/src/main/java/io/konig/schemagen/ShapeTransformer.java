package io.konig.schemagen;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * An interface that can transform shapes, typically for the purpose of constraining a physical
 * schema.
 * 
 * @author Greg McFall
 *
 */
public interface ShapeTransformer {
	
	/**
	 * Transform a PropertyConstraint for a given Shape
	 * @param shape  The shape whose PropertyConstraint is to be transformed
	 * @param constraint The constraint to be transformed
	 * @return The new version of the PropertyConstraint, or null if the property is to be omitted.
	 */
	PropertyConstraint transform(Shape shape, PropertyConstraint constraint);

}
