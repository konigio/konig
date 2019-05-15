package io.konig.core.showl;

import java.util.List;

import io.konig.shacl.Shape;

public interface ShowlTargetNodeShapeFactory {

	public List<ShowlNodeShape> createTargetNodeShapes(Shape shape) throws ShowlProcessingException;
}
