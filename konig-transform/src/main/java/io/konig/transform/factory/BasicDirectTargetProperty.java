package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

public class BasicDirectTargetProperty extends DirectTargetProperty {

	private SourceProperty preferredMatch;
	
	public BasicDirectTargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return preferredMatch;
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {
		this.preferredMatch = preferredMatch;
		preferredMatch.setMatch(this);
	}

}
