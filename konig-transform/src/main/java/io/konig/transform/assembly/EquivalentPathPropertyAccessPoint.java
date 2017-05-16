package io.konig.transform.assembly;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class EquivalentPathPropertyAccessPoint extends PropertyAccessPoint {

	private URI targetPredicate;
	private int pathIndex;
	
	public EquivalentPathPropertyAccessPoint(
		Shape sourceShape, 
		PropertyConstraint constraint, 
		int pathIndex, 
		URI targetPredicate
	) {
		super(sourceShape, constraint);
		this.pathIndex = pathIndex;
		this.targetPredicate = targetPredicate;
	}

	public int getPathIndex() {
		return pathIndex;
	}


	public URI getTargetPredicate() {
		return targetPredicate;
	}
	

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("targetPredicate", targetPredicate);
		out.field("pathIndex", pathIndex);
	}
}
