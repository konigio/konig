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
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;

public class ShowlPropertyShape implements Traversable {
	private ShowlNodeShape declaringShape;
	private ShowlProperty property;
	private PropertyConstraint propertyConstraint;
	private ShowlNodeShape valueShape;
	private ShowlPropertyGroup group;
	private Map<ShowlJoinCondition, ShowlMapping> mappings;
	
	public ShowlPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property, PropertyConstraint propertyConstraint) {
		this.declaringShape = declaringShape;
		this.property = property;
		this.propertyConstraint = propertyConstraint;
		property.addPropertyShape(this);
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
	
	public ShowlPropertyGroup getGroup() {
		return group;
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
		
		List<String> elements = new ArrayList<>();
		ShowlNodeShape node = null;
		for (ShowlPropertyShape p=this; p!=null; p=node.getAccessor()) {
			elements.add(p.getPredicate().getLocalName());
			node = p.getDeclaringShape();
		}
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		builder.append(RdfUtil.localName(node.getShape().getId()));
		builder.append('}');
		for (int i=elements.size()-1; i>=0; i--) {
			builder.append('.');
			builder.append(elements.get(i));
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
	
	public ShowlPropertyShape getPeer() {
		if (group!=null) {
			for (ShowlPropertyShape p : group) {
				if (p != this) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * This method should be used only by ShowlPropertyGroup
	 * @param group
	 */
	void group(ShowlPropertyGroup group) {
		this.group = group;
		
	}

	

}
