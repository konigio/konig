package io.konig.transform.factory;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

public class SourceProperty extends PropertyNode<SourceShape> {
	
	private TargetProperty match;
	private int pathIndex;

	public SourceProperty(PropertyConstraint propertyConstraint) {
		this(propertyConstraint, -1);
	}

	public SourceProperty(PropertyConstraint propertyConstraint, int pathIndex) {
		super(propertyConstraint);
		this.pathIndex = pathIndex;
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
		
	}

}
