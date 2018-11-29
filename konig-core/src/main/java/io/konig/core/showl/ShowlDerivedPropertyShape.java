package io.konig.core.showl;

public class ShowlDerivedPropertyShape extends ShowlPropertyShape {
	
	public ShowlDerivedPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property) {
		super(declaringShape, property, null);
	}
	
	@Override
	public ShowlDerivedPropertyShape asDerivedPropertyShape() {
		return this;
	}
}
