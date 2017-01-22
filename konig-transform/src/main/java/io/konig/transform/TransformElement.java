package io.konig.transform;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.Path;
import io.konig.shacl.Shape;

public class TransformElement {
	
	private Shape sourceShape;
	private List<PropertyTransform> propertyTransformList = new ArrayList<>();

	public TransformElement(Shape sourceShape) {
		this.sourceShape = sourceShape;
	}

	public Shape getSourceShape() {
		return sourceShape;
	}

	public void add(PropertyTransform transform) {
		propertyTransformList.add(transform);
	}

	public List<PropertyTransform> getPropertyTransformList() {
		return propertyTransformList;
	}
	
	public PropertyTransform getPropertyTansformByTargetPath(Path path) {
		for (PropertyTransform p : propertyTransformList) {
			Path targetPath = p.getTargetPath();
			if (targetPath.equals(path)) {
				return p;
			}
		}
		return null;
	}

	

}
