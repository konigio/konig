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


import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;

abstract public class PropertyModel {

	private URI predicate;
	private PropertyConstraint propertyConstraint;
	private ShapeModel valueModel;
	private PropertyGroup group;
	
	private ShapeModel declaringShape;

	public PropertyModel(URI predicate, PropertyConstraint propertyConstraint, PropertyGroup group) {
		this.predicate = predicate;
		this.propertyConstraint = propertyConstraint;
		this.group = group;
	}
	
	public boolean isTargetProperty() {
		return group.getTargetProperty()==this;
	}
	
	public boolean isSourceProperty() {
		return !isTargetProperty();
	}

	public URI getPredicate() {
		return predicate;
	}

	public PropertyGroup getGroup() {
		return group;
	}

	public void setGroup(PropertyGroup group) {
		this.group = group;
	}

	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}

	public ShapeModel getValueModel() {
		return valueModel;
	}

	public void setValueModel(ShapeModel valueModel) {
		this.valueModel = valueModel;
	}

	public ShapeModel getDeclaringShape() {
		return declaringShape;
	}

	public void setDeclaringShape(ShapeModel declaringShape) {
		this.declaringShape = declaringShape;
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

		builder.append("predicate=<");
		builder.append(predicate.stringValue());
		builder.append(">");
		if (declaringShape != null) {
			builder.append(", declaringShape.shape.id=<");
			builder.append(declaringShape.getShape().getId());
			builder.append(">");
		}
		if (valueModel != null) {
			builder.append(", valueModel.shape.id=");
			builder.append("<");
			builder.append(valueModel.getShape().getId().stringValue());
			builder.append(">");
		}
		
	}
	
}
