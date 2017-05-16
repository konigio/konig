package io.konig.transform.assembly;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * Encapsulates information about one method for accessing a given property.
 * @author Greg McFall
 *
 */
public class PropertyAccessPoint extends AbstractPrettyPrintable {

	private Shape sourceShape;
	private PropertyConstraint constraint;
	
	public PropertyAccessPoint(Shape sourceShape, PropertyConstraint constraint) {
		this.sourceShape = sourceShape;
		this.constraint = constraint;
	}

	public Shape getSourceShape() {
		return sourceShape;
	}

	public PropertyConstraint getConstraint() {
		return constraint;
	}
	
	public URI getTargetPredicate() {
		return constraint.getPredicate();
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("sourceShape", sourceShape.getId());
		out.beginObjectField("constraint", constraint);
		out.field("predicate", constraint.getPredicate());
		printLocalFields(out);
		out.endObjectField(constraint);
		out.endObject();
		
	}

	protected void printLocalFields(PrettyPrintWriter out) {
		
	}
	
}
