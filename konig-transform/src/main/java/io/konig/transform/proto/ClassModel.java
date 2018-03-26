package io.konig.transform.proto;

import java.util.ArrayList;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.io.BasePrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.formula.Direction;

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
	private Map<URI, PropertyGroup> outPropertyMap = new HashMap<>();
	private Map<URI, PropertyGroup> inPropertyMap = null;
	private ShapeModel targetShapeModel;
	private List<SourceShapeInfo> candidateSourceShapeModel;
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
	
	

	public void setOwlClass(URI owlClass) {
		this.owlClass = owlClass;
	}

	public URI getOwlClass() {
		return owlClass;
	}
	
	public PropertyGroup produceGroup(Direction direction, URI predicate) {
		
		if (direction == Direction.IN) {
			return produceInGroup(predicate);
		} else {
			return produceOutGroup(predicate);
		}
			
	}
	
	public PropertyGroup getInGroupByPredicate(URI predicate) {
		return inPropertyMap==null ? null : inPropertyMap.get(predicate);
	}
	
	public PropertyGroup produceInGroup(URI predicate) {
		if (inPropertyMap==null) {
			inPropertyMap = new HashMap<>();
		}
		PropertyGroup group = inPropertyMap.get(predicate);
		if (group == null) {
			group = new PropertyGroup();
			group.setParentClassModel(this);
			inPropertyMap.put(predicate, group);
		}
		return group;
	}
	
	public PropertyGroup produceOutGroup(URI predicate) {
		PropertyGroup group = outPropertyMap.get(predicate);
		if (group == null) {
			group = new PropertyGroup();
			group.setParentClassModel(this);
			outPropertyMap.put(predicate, group);
		}
		return group;
	}
	
	public PropertyGroup getGroupByPredicate(Direction direction, URI predicate) {
		if (direction == Direction.IN) {
			return getInGroupByPredicate(predicate);
		} else {
			return getOutGroupByPredicate(predicate);
		}
	}

	public PropertyGroup getOutGroupByPredicate(URI predicate) {
		return outPropertyMap.get(predicate);
	}
	
	public Collection<PropertyGroup> getOutGroups() {
		return outPropertyMap.values();
	}

	public ShapeModel getTargetShapeModel() {
		return targetShapeModel;
	}

	public void setTargetShapeModel(ShapeModel targetShapeModel) {
		this.targetShapeModel = targetShapeModel;
	}
	
	public boolean containsCandidateSourceShapeModel(ShapeModel candidate) {
		if (candidateSourceShapeModel != null) {
			for (SourceShapeInfo info : candidateSourceShapeModel) {
				if (info.getSourceShape()==candidate) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addCandidateSourceShapeModel(SourceShapeInfo info) {

		if (candidateSourceShapeModel == null) {
			candidateSourceShapeModel = new ArrayList<>();
		}
		for (SourceShapeInfo s : candidateSourceShapeModel) {
			if (s.getSourceShape() == info.getSourceShape()) {
				return;
			}
		}
		candidateSourceShapeModel.add(info);
	}
	
	public void addCandidateSourceShapeModel(ShapeModel candidate) {
		if (candidateSourceShapeModel == null) {
			candidateSourceShapeModel = new ArrayList<>();
		}
		for (SourceShapeInfo s : candidateSourceShapeModel) {
			if (s.getSourceShape() == candidate) {
				return;
			}
		}
		candidateSourceShapeModel.add(new SourceShapeInfo(candidate));
	}

	public List<SourceShapeInfo> getCandidateSourceShapeModel() {
		return candidateSourceShapeModel;
	}
	

	public void setCandidateSourceShapeModel(List<SourceShapeInfo> candidateSourceShapeModel) {
		this.candidateSourceShapeModel = candidateSourceShapeModel;
	}

	public ProtoFromItem getFromItem() {
		return fromItem;
	}

	public void setFromItem(ProtoFromItem fromItem) {
		this.fromItem = fromItem;
	}
	
	public boolean hasUnmatchedProperty() {
		for (PropertyGroup group : outPropertyMap.values()) {
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
		if (!outPropertyMap.isEmpty()) {
			out.beginArray("propertyGroup");
			for (PropertyGroup p : getOutGroups()) {
				out.print(p);
			}
			out.endArray("propertyGroup");
		}
		
		
	}
}
