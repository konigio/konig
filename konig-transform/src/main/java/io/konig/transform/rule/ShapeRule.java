package io.konig.transform.rule;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.sql.query.GroupingElement;
import io.konig.transform.proto.ShapeModel;

/**
 * A structure that describes the rules for transformation some shape (or set of shapes) to a given target shape.
 * @author Greg McFall
 */
public class ShapeRule extends AbstractPrettyPrintable {
	
	/**
	 * @deprecated Use fromItem instead
	 */
	private LinkedList<DataChannel> channels = new LinkedList<>();
	private FromItem fromItem;
	private Shape targetShape;
	private IdRule idRule;
	private Map<URI,PropertyRule> properties = new HashMap<>();

	private PropertyRule accessor;
	private VariableNamer variableNamer;
	private List<GroupingElement> groupingElement;
	
	private ShapeModel targetShapeModel;
	
	
	public ShapeRule(ShapeModel targetShapeModel) {
		this.targetShapeModel = targetShapeModel;
		this.targetShape = targetShapeModel.getShape();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<GroupingElement> getGroupingElement() {
		return groupingElement==null ? Collections.EMPTY_LIST : groupingElement;
	}



	public void addGroupingElement(GroupingElement groupingElement) {
		if (this.groupingElement == null) {
			this.groupingElement = new ArrayList<>();
		}
		this.groupingElement.add(groupingElement);
	}



	public List<DataChannel> getChannels() {
		return channels;
	}
	
	public void addChannel(DataChannel channel) {
		channels.addFirst(channel);
		channel.setParent(this);
	}
	
	public Shape getTargetShape() {
		return targetShape;
	}
	
	public IdRule getIdRule() {
		return idRule;
	}
	
	public void setIdRule(IdRule idRule) {
		this.idRule = idRule;
	}
	
	public void addPropertyRule(PropertyRule rule) {
		properties.put(rule.getPredicate(), rule);
		rule.setContainer(this);
	}
	
	public Collection<PropertyRule> getPropertyRules() {
		return properties.values();
	}

	public PropertyRule getAccessor() {
		return accessor;
	}

	public void setAccessor(PropertyRule accessor) {
		this.accessor = accessor;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("targetShape.id", targetShape.getId());
		out.field("idRule", idRule);
		if (!channels.isEmpty()) {
			out.beginArray("channels");
			for (DataChannel dc : channels) {
				out.print(dc);
			}
			out.endArray("channels");
		}
		if (!properties.isEmpty()) {
			out.beginArray("propertyRules");
			List<PropertyRule> list = new ArrayList<>(getPropertyRules());
			Collections.sort(list);
			
			for (PropertyRule p : list) {
				out.print(p);
			}
			out.endArray("propertyRules");
		}
		
		
	}

	public PropertyRule getProperty(URI predicate) {
		return properties.get(predicate);
	}

	public List<DataChannel> getAllChannels() {
		Set<ShapeRule> ruleSet = new HashSet<>();
		addAll(ruleSet, this);
		
		Set<DataChannel> set = new HashSet<>();
		for (ShapeRule s : ruleSet) {
			set.addAll(s.getChannels());
		}
		List<DataChannel> list = new ArrayList<>(set);
		
		Collections.sort(list);
		
		return list;
	}

	private void addAll(Set<ShapeRule> ruleSet, ShapeRule shapeRule) {
		
		if (!ruleSet.contains(shapeRule)) {
			ruleSet.add(shapeRule);
			for (PropertyRule p : shapeRule.getPropertyRules()) {
				ShapeRule nested = p.getNestedRule();
				if (nested != null) {
					addAll(ruleSet, nested);
				}
			}
		}
		
	}

	public VariableNamer getVariableNamer() {
		return variableNamer;
	}

	public void setVariableNamer(VariableNamer variableNamer) {
		this.variableNamer = variableNamer;
	}



	public FromItem getFromItem() {
		return fromItem;
	}

	public void setFromItem(FromItem fromItem) {
		this.fromItem = fromItem;
	}



	public ShapeModel getTargetShapeModel() {
		return targetShapeModel;
	}
	
	
}
