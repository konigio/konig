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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.io.BasePrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.formula.Direction;
import io.konig.transform.rule.ResultSet;

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
	private ProtoResultSetRule resultSetRule;
	private List<SourceShapeInfo> candidateSources;
	private List<SourceShapeInfo> committedSources;
	private ProtoFromItem fromItem;
	private int id=counter++;

	
	public ClassModel(URI owlClass) {
		this.owlClass = owlClass;
	}
	
	public ProtoFromItem findFromItem(ShapeModel shapeModel) {
		ProtoFromItemIterator sequence = new ProtoFromItemIterator(fromItem);
		while (sequence.hasNext()) {
			ProtoFromItem item = sequence.next();
			if (item == shapeModel) {
				return shapeModel;
			}
			if (item instanceof ProtoJoinExpression) {
				ProtoJoinExpression join = (ProtoJoinExpression) item;
				if (join.getLeft() == shapeModel || join.getRight()==shapeModel) {
					return join;
				}
			}
		}
		return null;
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
		if (candidateSources != null) {
			for (SourceShapeInfo info : candidateSources) {
				if (info.getSourceShape()==candidate) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addCandidateSourceShapeModel(SourceShapeInfo info) {

		if (candidateSources == null) {
			candidateSources = new ArrayList<>();
		}
		for (SourceShapeInfo s : candidateSources) {
			if (s.getSourceShape() == info.getSourceShape()) {
				return;
			}
		}
		candidateSources.add(info);
	}
	
	/**
	 * @deprecated
	 * @param candidate
	 */
	public void addCandidateSourceShapeModel(ShapeModel candidate) {
		if (candidateSources == null) {
			candidateSources = new ArrayList<>();
		}
		for (SourceShapeInfo s : candidateSources) {
			if (s.getSourceShape() == candidate) {
				return;
			}
		}
		SourceShapeInfo info = new SourceShapeInfo(candidate);
		candidateSources.add(info);
	}
	
	public void add(SourceShapeInfo info) {

		if (candidateSources == null) {
			candidateSources = new ArrayList<>();
		}
		ShapeModel candidate = info.getSourceShape();
		for (SourceShapeInfo s : candidateSources) {
			if (s.getSourceShape() == candidate) {
				return;
			}
		}
		candidateSources.add(info);
	}

	public void addCommittedSource(SourceShapeInfo info) {
		if (committedSources == null) {
			committedSources = new ArrayList<>();
		}
		committedSources.add(info);
	}
	
	public List<SourceShapeInfo> getCommittedSources() {
		return committedSources == null ? Collections.emptyList() : committedSources;
	}

	public List<SourceShapeInfo> getCandidateSources() {
		return candidateSources;
	}
	

	public void setCandidateSourceShapeModel(List<SourceShapeInfo> candidateSourceShapeModel) {
		this.candidateSources = candidateSourceShapeModel;
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

	public void removeGroup(Direction direction, URI predicate) {
		
		Map<URI,PropertyGroup> map = direction==Direction.OUT ? outPropertyMap : inPropertyMap;
		if (map != null) {
			map.remove(predicate);
		}
		
	}

	public ProtoResultSetRule getResultSetRule() {
		return resultSetRule;
	}

	public void setResultSetRule(ProtoResultSetRule resultSetRule) {
		this.resultSetRule = resultSetRule;
	}

	

	
}
