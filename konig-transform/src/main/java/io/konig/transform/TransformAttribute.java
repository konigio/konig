package io.konig.transform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class TransformAttribute {
	
	private PropertyConstraint property;
	private Map<Shape, MappedProperty> propertyMap = new HashMap<>();

	private TransformFrame embeddedFrame;
	
	
	public TransformAttribute(PropertyConstraint p) {
		this.property = p;
	}
	
	public PropertyConstraint getTargetProperty() {
		return this.property;
	}

	public URI getPredicate() {
		return property.getPredicate();
	}
	
	public void add(MappedProperty m) {
		propertyMap.put(m.getSourceShape(), m);
	}

	public TransformFrame getEmbeddedFrame() {
		return embeddedFrame;
	}

	public void setEmbeddedFrame(TransformFrame embeddedFrame) {
		this.embeddedFrame = embeddedFrame;
	}
	
	public MappedProperty getMappedProperty(Shape sourceShape) {
		return propertyMap.get(sourceShape);
	}
	
	
	public MappedProperty getMappedProperty() {
		Iterator<MappedProperty> sequence = propertyMap.values().iterator();
		return sequence.hasNext() ? sequence.next() : null;
	}
	

}
