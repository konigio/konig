package io.konig.transform.rule;

import io.konig.transform.ShapeTransformException;

public interface TransformPostProcessor {

	public void process(ShapeRule shapeRule) throws ShapeTransformException;
}
