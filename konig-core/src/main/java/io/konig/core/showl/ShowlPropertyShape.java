package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public abstract class ShowlPropertyShape implements Traversable {
	private static final Logger logger = LoggerFactory.getLogger(ShowlPropertyShape.class);
	private ShowlNodeShape declaringShape;
	private ShowlProperty property;
	private PropertyConstraint propertyConstraint;
	private ShowlNodeShape valueShape;
	private ShowlPropertyShape peer;
	private Map<ShowlJoinCondition, ShowlMapping> mappings;
	private Map<ShowlPropertyShape, ShowlJoinCondition> joinConditions;
	private ShowlMapping selectedMapping;
	private Set<Value> hasValue;
	
	public ShowlPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property, PropertyConstraint propertyConstraint) {
		this.declaringShape = declaringShape;
		this.property = property;
		this.propertyConstraint = propertyConstraint;
	}
	
	public NodeKind getNodeKind() {
		return propertyConstraint == null ? null : propertyConstraint.getNodeKind();
	}
	public void addMapping(ShowlMapping mapping) {
		if (mappings==null) {
			mappings = new HashMap<>();
		}
		mappings.put(mapping.getJoinCondition(), mapping);
	}
	
	public Collection<ShowlMapping> getMappings() {
		return mappings==null ? Collections.emptySet() : mappings.values();
	}
	
	public Collection<ShowlJoinCondition> listJoinConditions() {
		
		Set<ShowlJoinCondition> set = new HashSet<>();
		for (ShowlMapping m : getMappings()) {
			set.add(m.getJoinCondition());
		}
		return set;
	}
	
	/**
	 * Get the mapping for this property within the given join condition.
	 * @param joinCondition The join condition for which the mapping is requested.
	 * @return The mapping for this property within the join condition, or null if no mapping exists.
	 */
	public ShowlMapping getMapping(ShowlJoinCondition joinCondition) {
		return mappings==null ? null : mappings.get(joinCondition);
	}
	
	/**
	 * Returns true if this PropertyShape describes a direct property (i.e. not derived).
	 */
	public boolean isDirect() {
		return true;
	}
	
	public boolean isLeaf() {
		return valueShape==null;
	}
	
	public ShowlDerivedPropertyShape asDerivedPropertyShape() {
		return null;
	}
	
	public ShowlNodeShape getDeclaringShape() {
		return declaringShape;
	}
	
	public ShowlProperty getProperty() {
		return property;
	}
	
	public URI getPredicate() {
		return property.getPredicate();
	}
	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}
	
	public boolean isNestedAccordingToFormula() {
		if (propertyConstraint != null) {
			QuantifiedExpression formula = propertyConstraint.getFormula();
			if (formula != null) {
				PrimaryExpression primary = formula.asPrimaryExpression();
				if (primary instanceof PathExpression) {
					PathExpression path = (PathExpression) primary;
					int count = 0;
					for (PathStep step : path.getStepList()) {
						if (step instanceof DirectionStep) {
							if (++count==2) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public String getPath() {
		
		List<ShowlPropertyShape> elements = new ArrayList<>();
		ShowlNodeShape node = null;
		for (ShowlPropertyShape p=this; p!=null; p=node.getAccessor()) {
			elements.add(p);
			node = p.getDeclaringShape();
		}
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		builder.append(RdfUtil.localName(node.getShape().getId()));
		builder.append('}');
		for (int i=elements.size()-1; i>=0; i--) {
			ShowlPropertyShape p = elements.get(i);
			builder.append(p.pathSeparator());
			builder.append(p.getPredicate().getLocalName());
		}
		
		return builder.toString();
	}
	

	public String toString() {
		return getPath();
	}

	public ShowlNodeShape getValueShape() {
		return valueShape;
	}

	public void setValueShape(ShowlNodeShape valueShape) {
		this.valueShape = valueShape;
	}
	
	public ShowlNodeShape getRootNode() {
		ShowlNodeShape root = declaringShape;
		while (root.getAccessor() != null) {
			root = root.getAccessor().getDeclaringShape();
		}
		return root;
	}
	
	public ShowlClass getValueType(ShowlManager manager) {
		if (propertyConstraint != null) {
			URI valueClass = RdfUtil.uri(propertyConstraint.getValueClass());
			if (valueClass == null) {
				Shape shape = propertyConstraint.getShape();
				if (shape != null) {
					valueClass = shape.getTargetClass();
				}
			}
			if (valueClass != null) {
				return manager.produceOwlClass(valueClass);
			}
		}
		return property.inferRange(manager);
	}
	
	/**
	 * Returns the Property Shape linked to this one via a Formula.
	 * The link connects the PropertyShape that declares the Formula to the tail of the path in the formula.
	 */
	public ShowlPropertyShape getPeer() {
		return peer;
	}
	
	/**
	 * Sets the Property Shape linked to this one via a Formula, and visa versa.
	 * The link connects the PropertyShape that declares the Formula to the tail of the path in the formula.
	 * 
	 */
	void setPeer(ShowlPropertyShape peer) {
		if (logger.isDebugEnabled()) {
			logger.debug("setPeer: {} <=> {}", this.getPath(), peer.getPath());
		}
		this.peer = peer;
		peer.peer = this;
	}
	
	public void addJoinCondition(ShowlJoinCondition join) {
		if (joinConditions == null) {
			joinConditions = new HashMap<>();
		}
		ShowlPropertyShape other = join.otherProperty(this);
		if (other != null) {
			joinConditions.put(other, join);
		} else {
			throw new IllegalArgumentException();
		}
		
	}
	
	/**
	 * Check whether there exists a join condition involving this property and some other property.
	 * @param otherProperty  The other property in the join condition.
	 * @return The join condition if one exists; otherwise return null.
	 */
	public ShowlJoinCondition findJoinCondition(ShowlPropertyShape otherProperty) {
		return joinConditions==null ? null : joinConditions.get(otherProperty);
	}


	public char pathSeparator() {
		return '.';
	}
	
	public Direction getDirection() {
		return Direction.OUT;
	}

	public ShowlMapping getSelectedMapping() {
		return selectedMapping;
	}

	public void setSelectedMapping(ShowlMapping selectedMapping) {
		this.selectedMapping = selectedMapping;
	}

	public Set<Value> getHasValue() {
		return hasValue==null ? Collections.emptySet() : hasValue;
	}
	
	public void addHasValue(Value value) {
		if (hasValue == null) {
			hasValue = new LinkedHashSet<>();
		}
		hasValue.add(value);
	}

}
