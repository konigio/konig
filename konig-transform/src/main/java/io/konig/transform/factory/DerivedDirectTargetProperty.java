package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

public class DerivedDirectTargetProperty extends DirectTargetProperty {

	public DerivedDirectTargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return null;
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {

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
