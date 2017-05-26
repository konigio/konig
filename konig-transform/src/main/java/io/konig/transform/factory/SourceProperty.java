package io.konig.transform.factory;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

public class SourceProperty extends PropertyNode<SourceShape> {
	
	private TargetProperty match;
	private int pathIndex;
	private boolean isDerived;
	
	// (predicate, value) pair is used only for a "has constraint" from an equivalent Path.
	private URI predicate;
	private Value value;

	public SourceProperty(PropertyConstraint propertyConstraint) {
		this(propertyConstraint, -1);
	}

	public SourceProperty(PropertyConstraint propertyConstraint, int pathIndex) {
		super(propertyConstraint);
		this.pathIndex = pathIndex;
	}
	
	public SourceProperty(PropertyConstraint propertyConstraint, int pathIndex, URI predicate, Value value) {
		this(propertyConstraint, pathIndex);
		this.predicate = predicate;
		this.value = value;
	}

	public TargetProperty getMatch() {
		return match;
	}

	public void setMatch(TargetProperty match) {
		this.match = match;
	}

	@Override
	public int getPathIndex() {
		return pathIndex;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("hasMatch", match!=null);
		out.field("pathIndex", pathIndex);
		out.field("predicate", predicate);
		out.field("value", value);
		out.field("isDerived", isDerived);
		
	}
	
	@Override
	public URI getPredicate() {
		return predicate==null ? super.getPredicate() : predicate;
	}

	public Value getValue() {
		return value;
	}

	public boolean isDerived() {
		return isDerived;
	}

	@Override
	public void setDerived(boolean isDerived) {
		this.isDerived = isDerived;
	}
	


}
