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
import java.util.Map.Entry;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.TransformPostProcessor;

public class ShapeModel extends AbstractPrettyPrintable implements ProtoFromItem {
	

	private ClassModel classModel;
	private Shape shape;
	
	private Map<URI,PropertyModel> propertyMap = new HashMap<>();
	private Map<URI,VariablePropertyModel> variableMap = new HashMap<>();
	private List<StepPropertyModel> stepProperties = null;
	private List<GroupByItem> groupBy;
	private List<InversePropertyLink> inversePropertyLinks = null;
	
	private PropertyModel accessor;
	
	private DataChannel dataChannel;
	
	private List<TransformPostProcessor> postProcessorList = null;
	
	private SourceShapeInfo sourceShapeInfo;
	
	
	
	public SourceShapeInfo getSourceShapeInfo() {
		return sourceShapeInfo;
	}

	public void setSourceShapeInfo(SourceShapeInfo sourceShapeInfo) {
		this.sourceShapeInfo = sourceShapeInfo;
	}

	public ShapeModel rootTargetShapeModel() {
		ClassModel rootClassModel = classModel.rootClassModel();
		return rootClassModel.getTargetShapeModel();
	}
	
	public ShapeModel(Shape shape) {
		this.shape = shape;
	}
	
	public void addPostProcessor(TransformPostProcessor processor) {
		if (postProcessorList == null) {
			postProcessorList = new ArrayList<>();
		}
		postProcessorList.add(processor);
	}
	
	@SuppressWarnings("unchecked")
	public List<TransformPostProcessor> getPostProcessorList() {
		return postProcessorList == null ? Collections.EMPTY_LIST : postProcessorList;
	}


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
	
	public void addStepProperty(StepPropertyModel step) {
		if (stepProperties == null) {
			stepProperties = new ArrayList<>();
		}
		stepProperties.add(step);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<StepPropertyModel> getStepProperties() {
		return stepProperties==null ? Collections.EMPTY_LIST : stepProperties;
	}
	
	
	public Collection<PropertyModel> allProperties() {
		List<PropertyModel> list = new ArrayList<>();
		list.addAll(propertyMap.values());
		if (stepProperties != null) {
			list.addAll(stepProperties);
		}
		
		return list;
	}

	public void add(PropertyModel p) {
		propertyMap.put(p.getPredicate(), p);
		if (p instanceof VariablePropertyModel) {
			VariablePropertyModel var = (VariablePropertyModel) p;
			if (variableMap == null) {
				variableMap = new HashMap<>();
			}
			variableMap.put(p.getPredicate(), var);
		}
	}
	
	public Collection<VariablePropertyModel> getVariables() {
		return variableMap == null ? null : variableMap.values();
	}
	
	
	public Collection<PropertyModel> getProperties() {
		return propertyMap.values();
	}
	
	public PropertyModel getPropertyByPredicate(URI predicate) {
		return propertyMap.get(predicate);
	}
	
	/**
	 * The property through which this shape is accessed, or null if this is a 
	 * top-level property.  
	 * @return A PropertyModel <code>x</code> such that 
	 * <code>x.propertyConstraint.valueShape == this.shape</code>
	 */
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
	
	
	protected void appendProperties(StringBuilder builder) {
		builder.append("shape.id=");
		builder.append(shape.getId().stringValue());
		
	}
	public DataChannel getDataChannel() {
		return dataChannel;
	}
	public void setDataChannel(DataChannel dataChannel) {
		this.dataChannel = dataChannel;
	}
	
	public String accessorPath() {
		return accessor == null ? "" :accessor.simplePath();
	}
	
	public String simpleName() {
		Resource id = shape.getId();
		if (id instanceof URI) {
			return ((URI) id).getLocalName();
		}
		return "null";
	}
	
	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		Resource shapeId = shape.getId();
		String shapeLocalName = shapeId instanceof URI ? ((URI)shapeId).getLocalName() : "null";
		out.field("shape.id.localName", shapeLocalName);
		out.beginArray("property");
		for (Entry<URI, PropertyModel> e : propertyMap.entrySet()) {
			URI predicate = e.getKey();
			PropertyModel p = e.getValue();
			out.beginObject(p);
			out.field("predicate.localName", predicate.stringValue());
			ShapeModel nested = p.getValueModel();
			if (nested != null) {
				out.field("valueModel", nested);
			}
			out.endObject();
		}
		out.endArray("property");
		out.endObject();
		
	}


	public void addGroupBy(GroupByItem item) {
		if (groupBy == null) {
			groupBy = new ArrayList<>();
		}
		groupBy.add(item);
	}

	@SuppressWarnings("unchecked")
	public List<GroupByItem> getGroupBy() {
		return groupBy==null ? Collections.EMPTY_LIST : groupBy;
	}


	@Override
	public ProtoFromItem first() {
		return this;
	}


	@Override
	public ProtoFromItem rest() {
		return null;
	}

	public List<InversePropertyLink> getInversePropertyLinks() {
		return inversePropertyLinks;
	}


	public void add(InversePropertyLink link) {
		if (inversePropertyLinks == null) {
			inversePropertyLinks = new ArrayList<>();
		}
		inversePropertyLinks.add(link);
	}
	
	
}
