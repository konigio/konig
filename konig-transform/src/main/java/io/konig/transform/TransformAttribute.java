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


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
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
public class TransformAttribute extends AbstractPrettyPrintable {
	
	private PropertyConstraint property;
	private Map<ShapePath, MappedProperty> propertyMap = new HashMap<>();

	private TransformFrame embeddedFrame;
	
	
	public TransformAttribute(PropertyConstraint p) {
		this.property = p;
	}
	
	public PropertyConstraint getTargetProperty() {
		return this.property;
	}
	
	public MappedProperty getProperty(ShapePath shapePath) {
		return propertyMap.get(shapePath);
	}

	public URI getPredicate() {
		return property.getPredicate();
	}
	
	public Set<ShapePath> getShapePaths() {
		return propertyMap.keySet();
	}
	
	public void add(MappedProperty m) {
		propertyMap.put(m.getShapePath(), m);
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
	
	public Collection<MappedProperty> getMappedProperties() {
		return propertyMap.values();
	}
	
	public MappedProperty getMappedProperty() {
		Iterator<MappedProperty> sequence = propertyMap.values().iterator();
		return sequence.hasNext() ? sequence.next() : null;
	}
	
	public MappedProperty bestProperty() {
		MappedProperty best = null;
		int max = -1;
		if (best == null) {
			for (MappedProperty m : propertyMap.values()) {
				int count = m.getShapePath().getCount();
				if (count > max) {
					best = m;
					max = count;
				}
			}
		}
		
		return best;
	}
	
	/**
	 * @deprecated
	 */
	public MappedProperty bestProperty0() {
		if (embeddedFrame != null) {
			ShapePath best = embeddedFrame.bestShape();
			if (best != null) {
				MappedProperty m = getProperty(best);
				if (m != null) {
					return m;
				}
			}
		}
		
	
		return propertyMap.isEmpty() ? null : propertyMap.values().iterator().next();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransformAttribute(");
		builder.append(property.getPredicate().stringValue());
		builder.append(")");
		return builder.toString();
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		
		out.fieldName("property");
		out.beginObject(property);
		out.field("predicate", property.getPredicate().stringValue());
		out.field("equivalentPath", property.getEquivalentPath());
		out.endObject();
		out.field("embeddedFrame", embeddedFrame);
	
		if (!propertyMap.isEmpty()) {
			out.fieldName("propertyMap");
			out.pushIndent();
			out.println();
			for (MappedProperty mp : propertyMap.values()) {
				out.indent();
				out.print(mp);
			}
			out.popIndent();
			
		}
		
		
		
		out.endObject();
		
		
	}
}
