package io.konig.transform.proto;

import io.konig.transform.ShapeTransformException;

public interface FormulaHandler {

	boolean handleFormula(PropertyGroupHandler groupHandler, PropertyModel targetProperty) throws ShapeTransformException;
}
