package io.konig.transform.factory;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

@SuppressWarnings("rawtypes")
abstract public class PropertyNode<T extends ShapeNode> extends AbstractPrettyPrintable {

	private T parent;
	private PropertyConstraint propertyConstraint;

	private T nestedShape;
	

	public PropertyNode(PropertyConstraint propertyConstraint) {
		this.propertyConstraint = propertyConstraint;
	}
	
	public boolean isDirectProperty() {
		return getPathIndex()<0;
	}
	
	public boolean isLeaf() {
		return nestedShape == null;
	}

	public T getParent() {
		return parent;
	}

	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}

	abstract public int getPathIndex();

	public T getNestedShape() {
		return nestedShape;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	public void setNestedShape(T nestedShape) {
		this.nestedShape = nestedShape;
		nestedShape.setAccessor(this);
	}
	
	public URI getPredicate() {
		int pathIndex = getPathIndex();
		return pathIndex<0 ? 
			propertyConstraint.getPredicate() :
			propertyConstraint.getEquivalentPath().asList().get(pathIndex).getPredicate();
	}
	

	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.beginObjectField("propertyConstraint", propertyConstraint);
		out.field("predicate", propertyConstraint.getPredicate());
		if (propertyConstraint.getEquivalentPath() != null) {
			out.field("equivalentPath", propertyConstraint.getEquivalentPath().toSimpleString());
		}
		out.endObjectField(propertyConstraint);
		out.field("nestedShape", nestedShape);
		out.field("isDirectProperty", isDirectProperty());
		
		printLocalFields(out);
		out.endObject();
	}

	abstract protected void printLocalFields(PrettyPrintWriter out);
}
