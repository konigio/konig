package io.konig.transform.proto;

import io.konig.transform.ShapeTransformException;

public interface MatchVisitor {
	
	void matchId(IdPropertyModel sourceProperty, IdPropertyModel targetProperty) throws ShapeTransformException;
	
	void match(PropertyModel sourceProperty, DirectPropertyModel targetProperty) throws ShapeTransformException;
	
	void noMatch(DirectPropertyModel sourceProperty);
	
	void handleValueModel(ShapeModel sourceShapeModel) throws ShapeTransformException;
}
