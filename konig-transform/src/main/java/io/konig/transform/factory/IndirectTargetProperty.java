package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

/**
 * A TargetProperty corresponding to a step in an equivalent path.
 * Thus, it has pathIndex >= 0.
 * 
 * @author Greg McFall
 *
 */
abstract public class IndirectTargetProperty extends TargetProperty {

	private int pathIndex;
	public IndirectTargetProperty(PropertyConstraint propertyConstraint, int pathIndex) {
		super(propertyConstraint);
		this.pathIndex = pathIndex;
	}
	
	@Override
	public int getPathIndex() {
		return pathIndex;
	}


	@Override
	public int totalPropertyCount() {
		return 0;
	}

	@Override
	public int mappedPropertyCount() {
		return 0;
	}
}
