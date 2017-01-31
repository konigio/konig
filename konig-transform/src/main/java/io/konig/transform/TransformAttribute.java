package io.konig.transform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A structure that prescribes a method for producing a given property on the 
 * target Shape from one or more source shapes.
 * <p>
 * A TransformAttribute has the following fields:
 * <ul>
 * 	<li> property:  The {@link PropertyConstraint} that identifies the property on the target Shape
 *  <li> propertyMap: A map that associates a given source Shape with a 
 *            {@link MappedProperty} which prescribes a method for producing the target property
 *            from the source Shape.
 *  <li> embeddedFrame: A {@link TransformFrame} that describes how to produce the shape of 
 *            the target property.  This field is not null only if the target property has
 *            a <code>sh:shape</code> attribute.
 * </ul>
 * </p>
 * @author Greg McFall
 *
 */
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
