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
	protected ShowlProperty property;
	private PropertyConstraint propertyConstraint;
	private ShowlNodeShape valueShape;
	private Map<ShowlJoinCondition, ShowlMapping> mappings;
	private Map<ShowlPropertyShape, ShowlJoinCondition> joinConditions;
	private ShowlMapping selectedMapping;
	private Set<Value> hasValue;
	private ShowlExpression selectedExpression;
	private Set<ShowlPropertyShape> peerGroup = new HashSet<>();
	
	private ShowlExpression formula;
	private Set<ShowlExpression> usedIn;
	
	private List<ShowlExpression> expressionList = new ArrayList<>();
	
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
	
	public ShowlClass getValueType(ShowlSchemaService schemaService) {
		if (propertyConstraint != null) {
			URI valueClass = RdfUtil.uri(propertyConstraint.getValueClass());
			if (valueClass == null) {
				Shape shape = propertyConstraint.getShape();
				if (shape != null) {
					valueClass = shape.getTargetClass();
				}
			}
			if (valueClass != null) {
				return schemaService.produceShowlClass(valueClass);
			}
		}
		return property.inferRange(schemaService);
	}
	
	/**
	 * Returns the Property Shape linked to this one via a Formula.
	 * The link connects the PropertyShape that declares the Formula to the tail of the path in the formula.
	 */
	public ShowlPropertyShape getPeer() {
		return peerGroup.size()==1 ? peerGroup.iterator().next() : null;
	}
	
	/**
	 * Sets the Property Shape linked to this one via a Formula, and visa versa.
	 * The link connects the PropertyShape that declares the Formula to the tail of the path in the formula.
	 * 
	 */
	void addPeer(ShowlPropertyShape peer) {
		if (logger.isDebugEnabled()) {
			logger.debug("addPeer: {} <=> {}", this.getPath(), peer.getPath());
		}
		peerGroup.add(peer);
		peer.peerGroup.add(this);
	}
	
	public Set<ShowlPropertyShape> getPeerGroup() {
		return peerGroup;
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

	public List<ShowlExpression> getExpressionList() {
		return expressionList;
	}

	public void addExpression(ShowlExpression expression) {
		if (logger.isTraceEnabled()) {
			logger.trace("{}.addExpression({})", getPath(), expression.displayValue());
		}
		expressionList.add(expression);
	}
	
	public boolean isDeclaredWithin(ShowlNodeShape node) {
		for (ShowlNodeShape parent=declaringShape; 
				parent!=null; 
				parent = parent.getAccessor()==null ? null : parent.getAccessor().getDeclaringShape()) {
		
			if (parent == node) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean inSameNodeShape(ShowlPropertyShape other) {
		return other.isDeclaredWithin(declaringShape) || isDeclaredWithin(other.getDeclaringShape());
	}
	
	public ShowlPropertyShape getSynonym() {
		for (ShowlExpression e : expressionList) {
			if (e instanceof ShowlPropertyExpression) {
				ShowlPropertyShape other = ((ShowlPropertyExpression) e).getSourceProperty();
				if (inSameNodeShape(other)) {
					return other;
				}
				
			}
		}
		return null;
	}

	public List<ShowlPropertyShape> propertyPath() {
		List<ShowlPropertyShape> list = new ArrayList<>();
		for (ShowlPropertyShape p = this; p!=null; p=p.getDeclaringShape().getAccessor()) {
			list.add(p);
		}
		Collections.reverse(list);
		return list;
	}

	/**
	 * Return the synonym of this property if the synonym is direct.  Otherwise, return this property.
	 */
	public ShowlPropertyShape maybeDirect() {
		if (this instanceof ShowlDirectPropertyShape) {
			return this;
		}
		ShowlPropertyShape synonym = getSynonym();
		if (synonym instanceof ShowlDirectPropertyShape) {
			return synonym;
		}
		return this;
	}

	public ShowlExpression getFormula() {
		return formula;
	}

	public void setFormula(ShowlExpression formula) {
		this.formula = formula;
	}

	/**
	 * Get the expression that was selected to construct the value for this property.
	 */
	public ShowlExpression getSelectedExpression() {
		return selectedExpression;
	}

	/**
	 * Set the expression that was selected to construct the value for this property.
	 */
	public void setSelectedExpression(ShowlExpression selectedExpression) {
		if (logger.isTraceEnabled()) {
			logger.trace("setSelectedExpression {} = {}", getPath(), selectedExpression.displayValue());
			System.out.print("");
		}
		this.selectedExpression = selectedExpression;
	}

	/**
	 * The set of 'selected' expressions in which this property appears.
	 * A 'selected' expression is one that is used in the mapping from source to target.
	 * If this set is non-empty, then it must be the case that this property is a source property.
	 */
	public Set<ShowlExpression> getUsedIn() {
		return usedIn==null ? Collections.emptySet() : usedIn;
	}

	/**
	 * Declared that this property appears in a given selected expression.
	 */
	public void usedIn(ShowlExpression e) {
		if (usedIn == null) {
			usedIn = new LinkedHashSet<>();
		}
		usedIn.add(e);
	}


}
