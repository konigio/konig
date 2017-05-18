package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

public class LeafIndirectTargetProperty extends IndirectTargetProperty {

	private SharedSourceProperty preferredMatch;
	
	public LeafIndirectTargetProperty(PropertyConstraint propertyConstraint, int pathIndex, SharedSourceProperty preferredMatch) {
		super(propertyConstraint, pathIndex);
		this.preferredMatch = preferredMatch;
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return preferredMatch.get();
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {
		this.preferredMatch.set(preferredMatch);
		preferredMatch.setMatch(this);
	}

}
