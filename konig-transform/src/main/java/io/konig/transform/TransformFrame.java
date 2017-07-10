package io.konig.transform;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
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
public class TransformFrame extends AbstractPrettyPrintable {
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
		if (best == null) {
			for (MappedId mappedId : idMap.values()) {
				best = mappedId.getShapePath();
				break;
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

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		out.fieldName("attributes");
		out.println();
		out.pushIndent();
		for (TransformAttribute a : attributes.values()) {
			out.indent();
			out.print(a);
		}
		out.popIndent();
		out.endObject();
		
	}

}
