package io.konig.transform.rule.handlers;

import io.konig.transform.ShapeTransformException;
import io.konig.transform.assembly.TransformEvent;

public class UnsupportedEventException extends ShapeTransformException {
	private static final long serialVersionUID = 1L;

	public UnsupportedEventException(TransformEvent event) {
		super("Unsupported event type: " + event.getType().name());
	}

}
