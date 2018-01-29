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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.io.BasePrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

/**
 * A model for the properties of a given OWL Class.
 * The model contains a collection of PropertyGroup entities which describe the 
 * mapping of properties from source shapes to the target shape.
 * @author Greg McFall
 *
 */
public class ClassModel extends BasePrettyPrintable {
	private static int counter = 0;

	private URI owlClass;
	private Map<URI, PropertyGroup> propertyMap = new HashMap<>();
	private ShapeModel targetShapeModel;
	private Set<ShapeModel> candidateSourceShapeModel;
	private ProtoFromItem fromItem;
	private int id=counter++;
	
	public ClassModel(URI owlClass) {
		this.owlClass = owlClass;
	}
	
	public ClassModel rootClassModel() {
		ClassModel parent = getParent();
		return parent==null ? this : parent.rootClassModel();
	}
	
	public ClassModel getParent() {
		PropertyModel accessor = targetShapeModel.getAccessor();
		if (accessor!=null) {
			return accessor.getDeclaringShape().getClassModel();
		}
		return null;
	}
	
	public int hashCode() {
		return id;
	}
	
	public void put(URI predicate, PropertyGroup group) {
		group.setParentClassModel(this);
		propertyMap.put(predicate, group);
	}
	

	public void setOwlClass(URI owlClass) {
		this.owlClass = owlClass;
	}

	public URI getOwlClass() {
		return owlClass;
	}
	
	public void putPropertGroup(URI predicate, PropertyGroup group) {
		propertyMap.put(predicate, group);
	}
	
	public PropertyGroup producePropertyGroup(URI predicate) {
		PropertyGroup group = propertyMap.get(predicate);
		if (group == null) {
			group = new PropertyGroup();
			put(predicate, group);
		}
		return group;
	}

	public PropertyGroup getPropertyGroupByPredicate(URI predicate) {
		return propertyMap.get(predicate);
	}
	
	public Collection<PropertyGroup> getPropertyGroups() {
		return propertyMap.values();
	}

	public ShapeModel getTargetShapeModel() {
		return targetShapeModel;
	}

	public void setTargetShapeModel(ShapeModel targetShapeModel) {
		this.targetShapeModel = targetShapeModel;
	}
	
	public void addCandidateSourceShapeModel(ShapeModel candidate) {
		if (candidateSourceShapeModel == null) {
			candidateSourceShapeModel = new HashSet<>();
		}
		candidateSourceShapeModel.add(candidate);
	}

	public Set<ShapeModel> getCandidateSourceShapeModel() {
		return candidateSourceShapeModel;
	}
	

	public void setCandidateSourceShapeModel(Set<ShapeModel> candidateSourceShapeModel) {
		this.candidateSourceShapeModel = candidateSourceShapeModel;
	}

	public ProtoFromItem getFromItem() {
		return fromItem;
	}

	public void setFromItem(ProtoFromItem fromItem) {
		this.fromItem = fromItem;
	}
	
	public boolean hasUnmatchedProperty() {
		for (PropertyGroup group : propertyMap.values()) {
			PropertyModel targetProperty = group.getTargetProperty();
			if (targetProperty == null) {
				continue;
			}
			ClassModel nested = group.getValueClassModel();
			
			if (
				(nested==null && group.getSourceProperty()==null) ||
				(nested!=null && nested.hasUnmatchedProperty())
			) {
				return true;
			}
		}
		return false;
	}


	@Override
	protected void printProperties(PrettyPrintWriter out) {
		out.field("owlClass", owlClass);
		out.field("fromItem", fromItem);
		if (!propertyMap.isEmpty()) {
			out.beginArray("propertyGroup");
			for (PropertyGroup p : getPropertyGroups()) {
				out.print(p);
			}
			out.endArray("propertyGroup");
		}
		
		
	}
}
