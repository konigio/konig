package io.konig.core.showl;

import io.konig.shacl.PropertyConstraint;

public class ShowlVariablePropertyShape extends ShowlOutwardPropertyShape {

	public ShowlVariablePropertyShape(ShowlNodeShape declaringShape, ShowlProperty property) {
		super(declaringShape, property);
	}

	public ShowlVariablePropertyShape(ShowlNodeShape declaringShape, ShowlProperty property, PropertyConstraint c) {
		super(declaringShape, property, c);
	}

}
