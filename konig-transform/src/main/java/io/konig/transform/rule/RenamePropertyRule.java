package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class RenamePropertyRule extends AbstractPropertyRule implements PropertyRule {
	
	private URI focusPredicate;
	private PropertyConstraint sourceProperty;
	private int pathIndex;

	public RenamePropertyRule(
		URI focusPredicate, 
		RankedVariable<Shape> sourceShape, 
		PropertyConstraint sourceProperty,
		int pathIndex
	) {
		this.focusPredicate = focusPredicate;
		this.sourceShape = sourceShape;
		this.sourceProperty = sourceProperty;
		this.pathIndex = pathIndex;
	}

	public int getPathIndex() {
		return pathIndex;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		
		out.field("targetPredicate", focusPredicate);
		
		out.beginObjectField("sourceShape", sourceShape);
		out.field("name", sourceShape.getName());
		out.field("value", sourceShape.getValue().getId());
		out.endObject();
		
		out.beginObjectField("sourceProperty", sourceProperty);
		out.field("predicate", sourceProperty.getPredicate());
		out.endObjectField(sourceProperty);
		
		out.endObject();

	}



	public PropertyConstraint getSourceProperty() {
		return sourceProperty;
	}



	@Override
	public URI getFocusPredicate() {
		return focusPredicate;
	}

	@Override
	public RankedVariable<Shape> getSourceShapeVariable() {
		return sourceShape;
	}

}
