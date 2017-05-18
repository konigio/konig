package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

/**
 * An IndirectTargetProperty that is not a leaf.
 * Consequently, nestedShape is not null.
 * <p>
 * This kind of TargetProperty does not accept a preferred match and will silently ignore
 * attempts to set a preferred match.
 * </p>
 * @author Greg McFall
 *
 */
public class ContainerIndirectTargetProperty extends IndirectTargetProperty {

	public ContainerIndirectTargetProperty(PropertyConstraint propertyConstraint, int pathIndex) {
		super(propertyConstraint, pathIndex);
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return null;
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {
	}

}
