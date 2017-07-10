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


import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

@SuppressWarnings("rawtypes")
abstract public class PropertyNode<T extends ShapeNode> extends AbstractPrettyPrintable {

	private T parent;
	private PropertyConstraint propertyConstraint;

	private T nestedShape;
	

	public PropertyNode(PropertyConstraint propertyConstraint) {
		this.propertyConstraint = propertyConstraint;
	}
	
	public boolean isDirectProperty() {
		return getPathIndex()<0;
	}
	
	public boolean isLeaf() {
		return nestedShape == null;
	}

	public T getParent() {
		return parent;
	}

	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}

	abstract public int getPathIndex();

	public T getNestedShape() {
		return nestedShape;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	public void setNestedShape(T nestedShape) {
		this.nestedShape = nestedShape;
		nestedShape.setAccessor(this);
	}
	
	public URI getPredicate() {
		int pathIndex = getPathIndex();
		return pathIndex<0 ? 
			propertyConstraint.getPredicate() :
			propertyConstraint.getEquivalentPath().asList().get(pathIndex).getPredicate();
	}
	

	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.beginObjectField("propertyConstraint", propertyConstraint);
		out.field("predicate", propertyConstraint.getPredicate());
		if (propertyConstraint.getEquivalentPath() != null) {
			out.field("equivalentPath", propertyConstraint.getEquivalentPath().toSimpleString());
		}
		out.endObjectField(propertyConstraint);
		out.field("nestedShape", nestedShape);
		out.field("isDirectProperty", isDirectProperty());
		
		printLocalFields(out);
		out.endObject();
	}
	
	public void setDerived(boolean isDerived) {
		// Do nothing by default
	}

	abstract protected void printLocalFields(PrettyPrintWriter out);
}
