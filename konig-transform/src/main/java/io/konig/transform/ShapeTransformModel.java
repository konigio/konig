package io.konig.transform;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Resource;

import io.konig.shacl.Shape;

public class ShapeTransformModel  {
	
	private Shape targetShape;
	private Map<Resource, TransformElement> sourceMap = new LinkedHashMap<>();
	
	public ShapeTransformModel(Shape targetShape) {
		this.targetShape = targetShape;
	}
	
	
	public void add(TransformElement source) {
		Resource key = source.getSourceShape().getId();
		sourceMap.put(key, source);
	}
	
	public TransformElement getGeneratorSourceByShapeId(Resource id) {
		return sourceMap.get(id);
	}
	
	public Collection<TransformElement> getElements() {
		return sourceMap.values();
	}


	public Shape getTargetShape() {
		return targetShape;
	}

}
