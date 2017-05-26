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


	@Override
	public int totalPropertyCount() {
		TargetShape nested = getNestedShape();
		return nested == null ? 1 : nested.totalPropertyCount();
	}

	@Override
	public int mappedPropertyCount() {
		TargetShape nested = getNestedShape();
		return nested != null ? 
			nested.mappedPropertyCount() :
			getPreferredMatch()!=null ? 1 :
			0;
	}

}
