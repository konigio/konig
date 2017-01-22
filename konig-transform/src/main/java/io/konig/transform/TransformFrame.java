package io.konig.transform;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A structure that describes the transformation from one shape (or set of shapes) to another.
 * @author Greg McFall
 *
 */
public class TransformFrame {
	private Shape targetShape;
	private Map<URI, TransformAttribute> attributes = new LinkedHashMap<>();
	private Map<Shape, MappedId> idMap = new HashMap<>();
	
	
	public TransformFrame(Shape targetShape) {
		this(targetShape, new HashMap<Shape,TransformFrame>());
	}
	
	public void addIdMapping(MappedId m) {
		idMap.put(m.getSourceShape(), m);
	}
	
	public MappedId getMappedId() {
		Iterator<MappedId> sequence = idMap.values().iterator();
		return sequence.hasNext() ? sequence.next() : null;
	}
	
	public Collection<MappedId> getIdMappings() {
		return idMap.values();
	}
	
	public MappedId getIdMapping(Shape sourceShape) {
		return idMap.get(sourceShape);
	}
	
	private TransformFrame(Shape targetShape, Map<Shape,TransformFrame> shapeMap) {
		this.targetShape = targetShape;
		shapeMap.put(targetShape, this);
		
		for (PropertyConstraint p : targetShape.getProperty()) {
			
			URI predicate = p.getPredicate();
			if (predicate != null) {
				TransformAttribute attr = new TransformAttribute(p);
				attributes.put(predicate, attr);
				
				Shape valueShape = p.getShape();
				if (valueShape != null) {
					TransformFrame embeddedFrame = shapeMap.get(valueShape);
					if (embeddedFrame == null) {
						embeddedFrame = new TransformFrame(valueShape, shapeMap);
					}
					attr.setEmbeddedFrame(embeddedFrame);
				}
			}
		}
		
	}
	
	public TransformAttribute getAttribute(URI predicate) {
		return attributes.get(predicate);
	}

	public Shape getTargetShape() {
		return targetShape;
	}
	
	public Collection<TransformAttribute> getAttributes() {
		return attributes.values();
	}
	

}
