package io.konig.transform.assembly;

import io.konig.transform.ShapeTransformException;

public interface TransformEventHandler {

	void handle(TransformEvent event) throws ShapeTransformException;
	
	/**
	 * Get the types of events to which this handler responds.
	 */
	TransformEventType[] respondsTo();
}
