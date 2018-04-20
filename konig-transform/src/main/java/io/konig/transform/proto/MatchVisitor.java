package io.konig.transform.proto;

public interface MatchVisitor {
	
	void match(PropertyModel sourceProperty, DirectPropertyModel targetProperty);
	
	void noMatch(DirectPropertyModel sourceProperty);
	
	void handleValueModel(ShapeModel sourceShapeModel);
}
