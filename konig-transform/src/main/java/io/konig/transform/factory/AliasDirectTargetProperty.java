package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

public class AliasDirectTargetProperty extends DirectTargetProperty {

	private SharedSourceProperty preferredMatch;
	
	public AliasDirectTargetProperty(PropertyConstraint propertyConstraint, SharedSourceProperty preferredMatch) {
		super(propertyConstraint);
		this.preferredMatch = preferredMatch;
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return preferredMatch.get();
	}

	@Override
	public void setPreferredMatch(SourceProperty value) {
		preferredMatch.set(value);
		value.setMatch(this);
	}

}
