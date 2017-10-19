package io.konig.transform.proto;

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

import io.konig.shacl.Shape;

public class ShapeModel implements ProtoFromItem {

	private ClassModel classModel;
	private Shape shape;
	private Map<URI,PropertyModel> propertyMap = new HashMap<>();
	private PropertyModel accessor;
	
	public ClassModel getClassModel() {
		return classModel;
	}
	public void setClassModel(ClassModel classModel) {
		this.classModel = classModel;
	}
	public Shape getShape() {
		return shape;
	}
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public void add(PropertyModel p) {
		propertyMap.put(p.getPredicate(), p);
	}
	
	public Collection<PropertyModel> getProperties() {
		return propertyMap.values();
	}
	
	public PropertyModel getPropertyByPredicate(URI predicate) {
		return propertyMap.get(predicate);
	}
	public PropertyModel getAccessor() {
		return accessor;
	}
	public void setAccessor(PropertyModel accessor) {
		this.accessor = accessor;
	}
	
	public boolean isTargetShape() {
		return classModel.getTargetShapeModel()==this;
	}
	
	public boolean isSourceShape() {
		return !isTargetShape();
	}
	
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append('[');
		appendProperties(builder);
		builder.append(']');
		return builder.toString();
	}
	protected void appendProperties(StringBuilder builder) {
		builder.append("shape.id=");
		builder.append(shape.getId().stringValue());
		
	}
	
}
