package io.konig.transform.factory;

import io.konig.shacl.PropertyConstraint;

public abstract class DirectTargetProperty extends TargetProperty {

	public DirectTargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}

	@Override
	public boolean isDirectProperty() {
		return true;
	}

	@Override
	public int getPathIndex() {
		return -1;
	}

}
