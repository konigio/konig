package io.konig.transform.factory;

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
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

@SuppressWarnings("rawtypes")
abstract public class ShapeNode<P extends PropertyNode> extends AbstractPrettyPrintable {

	private P accessor;
	private Shape shape;
	
	protected Map<URI,P> properties = new HashMap<>();
	
	public ShapeNode(Shape shape) {
		this.shape = shape;
	}
	
	@SuppressWarnings("unchecked")
	public void add(P node) {
//		System.out.println("ShapeNode.add " + this.hashCode() + " " + node.getPredicate());
		properties.put(node.getPredicate(), node);
		node.setParent(this);
	}


	public P getAccessor() {
		return accessor;
	}


	public void setAccessor(P accessor) {
		this.accessor = accessor;
	}


	public Shape getShape() {
		return shape;
	}

	public Collection<P> getProperties() {
		return properties.values();
	}
	
	public P getProperty(URI predicate) {
		return properties.get(predicate);
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		
		if (shape != null) {
			out.beginObjectField("shape", shape);
			out.field("id", shape.getId());
			out.endObjectField(shape);
		}
		
		if (!properties.isEmpty()) {
			out.beginArray("properties");
			for (P p : getProperties()) {
				out.print(p);
			}
			out.endArray("properties");
		}
		if (accessor != null) {
			out.beginObjectField("accessor", accessor);
			out.field("predicate", accessor.getPredicate());
			out.endObjectField(accessor);
		}
		
		printLocalFields(out);
		
		out.endObject();
		
	}

	abstract protected void printLocalFields(PrettyPrintWriter out);
}
