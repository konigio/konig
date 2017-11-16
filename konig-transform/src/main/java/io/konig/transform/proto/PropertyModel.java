package io.konig.transform.proto;

import java.util.ArrayList;
import java.util.List;

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

import io.konig.core.io.BasePrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

abstract public class PropertyModel extends BasePrettyPrintable implements GroupByItem {

	private URI predicate;
	private PropertyGroup group;
	
	private ShapeModel declaringShape;

	public PropertyModel(URI predicate, PropertyGroup group) {
		this.predicate = predicate;
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

	public ShapeModel getDeclaringShape() {
		return declaringShape;
	}

	public void setDeclaringShape(ShapeModel declaringShape) {
		this.declaringShape = declaringShape;
	}

	public abstract ShapeModel getValueModel();

	
	
	public String simplePath() {
		List<String> list = new ArrayList<>();
		PropertyModel p = this;
		while (p != null) {
			URI predicate = p.getPredicate();
			list.add(predicate.getLocalName());
			ShapeModel shapeModel = p.getDeclaringShape();
			p = shapeModel.getAccessor();
		}
		
		StringBuilder builder = new StringBuilder();
		String dot = "";
		for (int i=list.size()-1; i>=0; i--) {
			builder.append(dot);
			builder.append(list.get(i));
			dot = ".";
		}
		return builder.toString();
	}


	@Override
	protected void printProperties(PrettyPrintWriter out) {

		if (declaringShape!=null) {
			out.beginObjectField("declaringShape", declaringShape);
			out.field("shape.id", declaringShape.getShape().getId());
			if (declaringShape.getDataChannel()!=null) {
				out.beginObjectField("dataChannel", declaringShape.getDataChannel());
				out.field("name", declaringShape.getDataChannel().getName());
				out.endObjectField(declaringShape.getDataChannel());
			}
			out.endObjectField(declaringShape);
			
		}
		out.field("predicate", predicate);
		
	}
	
}
