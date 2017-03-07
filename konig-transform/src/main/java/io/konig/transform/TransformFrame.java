package io.konig.transform;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A structure that describes the transformation from one shape (or set of shapes) to another.
 * <p>
 * A TransformFrame has the following fields:
 * <ul>
 *   <li> targetShape: The Shape that is to be produced by the transform process.
 *   <li> idMap: A map from a given source shape to a {@link MappedId} structure that 
 *   	prescribes a method for producing the IRI for the target.
 *   <li> attributes: A map from the URI for the predicate of a property on the targetShape to a 
 *   	{@link TransformAttribute} which prescribes a method for producing the specified property 
 *      from some source Shape.
 * </ul>
 * </p>
 * @author Greg McFall
 *
 */
public class TransformFrame {
	private Shape targetShape;
	private Map<URI, TransformAttribute> attributes = new LinkedHashMap<>();
	private Map<Shape, MappedId> idMap = new HashMap<>();
	private boolean countedShapes = false;
	
	
	public TransformFrame(Shape targetShape) {
		this.targetShape = targetShape;
	}
	
	public void addAttribute(TransformAttribute attr) {
		attributes.put(attr.getPredicate(), attr);
	}
	
	public void addIdMapping(MappedId m) {
		idMap.put(m.getSourceShape(), m);
	}
	
	public MappedId getMappedId() {
		Iterator<MappedId> sequence = idMap.values().iterator();
		return sequence.hasNext() ? sequence.next() : null;
	}
	
	/**
	 * Get the ShapePath that covers the most attributes
	 */
	public ShapePath bestShape() {
		countShapes();
		ShapePath best = null;
		int bestCount = 0;
		for (TransformAttribute attr : getAttributes()) {
			for (ShapePath s : attr.getShapePaths()) {
				if (s.getCount() > bestCount) {
					bestCount = s.getCount();
					best = s;
				}
			}
		}
		return best;
	}
	
	public Collection<MappedId> getIdMappings() {
		return idMap.values();
	}
	
	public MappedId getIdMapping(Shape sourceShape) {
		return idMap.get(sourceShape);
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
	
	public String toString() {
		StringWriter buffer = new StringWriter();
		TransformFrameWriter writer = new TransformFrameWriter(buffer);
		writer.write(this);
		
		
		return buffer.toString();
	}
	
	public void countShapes() {
		if (!countedShapes) {
			countedShapes = true;
			for (TransformAttribute attr : getAttributes()) {
				Set<ShapePath> set = attr.getShapePaths();
				for (ShapePath s : set) {
					s.incrementCount();
					TransformFrame child = attr.getEmbeddedFrame();
					if (child != null) {
						child.countShapes();
					}
				}
			}
		}
	}

}
