package io.konig.transform.factory;

import io.konig.transform.ShapeTransformException;

public class TransformBuildException extends ShapeTransformException {
	private static final long serialVersionUID = 1L;

	public TransformBuildException(String arg0) {
		super(arg0);
	}

	public TransformBuildException(Throwable arg0) {
		super(arg0);
	}

	public TransformBuildException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
