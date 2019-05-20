package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;

@SuppressWarnings("serial")
public class ShowlPropertyShapeGroup extends ArrayList<ShowlPropertyShape> {

	private ShowlEffectiveNodeShape declaringShape;
	private URI predicate;

	private ShowlEffectiveNodeShape valueShape;
	private ShowlExpression selectedExpression;
	
	public ShowlPropertyShapeGroup(ShowlEffectiveNodeShape declaringShape, URI predicate) {
		this.declaringShape = declaringShape;
		this.predicate = predicate;
	}

	public ShowlEffectiveNodeShape getValueShape() {
		return valueShape;
	}

	public void setValueShape(ShowlEffectiveNodeShape valueShape) {
		this.valueShape = valueShape;
	}

	public ShowlEffectiveNodeShape getDeclaringShape() {
		return declaringShape;
	}

	public URI getPredicate() {
		return predicate;
	}
	
	/**
	 * Find the ShowlPropertyShape that has a selected expression.
	 * @return
	 */
	public ShowlPropertyShape withSelectedExpression() {
		
		for (ShowlPropertyShape p : this) {
			if (p.getSelectedExpression() != null) {
				return p;
			}
		}
		
		return null;
	}
	
	public ShowlManager getShowlManager() {
		return isEmpty() ? null : get(0).getDeclaringShape().getShowlManager();
	}
	
	/**
	 * Return the range of this property, or null if the range is not known.
	 * This method first checks for a direct property if one exists and returns the range declared by the direct property.
	 * If there is no direct property, this method seeks the most general range as declared by the derived property. If no
	 * derived properties exist, or no derived properties specify the range, then this method returns the range as inferred 
	 * from the ShowlProperty.
	 * @return
	 */
	public ShowlClass range() {
		ShowlClass result = null;
		
		// Try direct property
		ShowlDirectPropertyShape direct = direct();
		if (direct != null) {
			result = rangeClass(direct);
		}
		if (result == null) {
			// Try derived property
			
			for (ShowlPropertyShape p : this) {
				ShowlClass range = rangeClass(p);
				if (range != null) {
					if (result == null) {
						result = range;
					} else {
						result = mostGeneralType(result, range);
					}
				}
			}
		}
		
		if (result == null) {
			// Try ShowlProperty
			ShowlManager manager = getShowlManager();
			ShowlProperty property = manager.produceShowlProperty(predicate);
			result = property.inferRange(manager.getShowlFactory());
		}
		return result;
	}
	
	private ShowlClass mostGeneralType(ShowlClass a, ShowlClass b) {
		
		OwlReasoner reasoner = getShowlManager().getReasoner();
		
		return reasoner.isSubClassOf(a.getId(), b.getId()) ? b : a;
	}

	private ShowlClass rangeClass(ShowlPropertyShape p) {
		
		PropertyConstraint c = p.getPropertyConstraint();
		if (c != null) {
			URI rangeId = c.getDatatype();
			if (rangeId == null) {
				rangeId = RdfUtil.uri(c.getValueClass());
			}
			if (rangeId == null && c.getShape()!=null) {
				rangeId = c.getShape().getTargetClass();
			}
			if (rangeId != null) {
				return getShowlManager().produceOwlClass(rangeId);
			}
		}
		return null;
	}

	/**
	 * Find a direct property for the current predicate. 
	 * @return The ShowlDirectProperty for the current predicate, if one exists and null otherwise.
	 */
	public ShowlDirectPropertyShape direct() {
		for (ShowlPropertyShape p : this) {
			if (p instanceof ShowlDirectPropertyShape) {
				return (ShowlDirectPropertyShape) p;
			}
		}
		return null;
	}
	
	public ShowlDirectPropertyShape synonymDirect() {

		for (ShowlPropertyShape p : this) {
			if (p instanceof ShowlDirectPropertyShape) {
				return (ShowlDirectPropertyShape) p;
			}
			
			ShowlPropertyShape synonym = p.getSynonym();
			if (synonym instanceof ShowlDirectPropertyShape) {
				return (ShowlDirectPropertyShape) synonym;
			}
		}
		return null;
	}
	
	/**
	 * Compute the sequence of properties from the root that leads to this property.
	 */
	public List<ShowlPropertyShapeGroup> path() {
		List<ShowlPropertyShapeGroup> result = new ArrayList<>();
		for (ShowlPropertyShapeGroup p=this; p!=null; p=p.getDeclaringShape().getAccessor()) {
			result.add(p);
		}
		Collections.reverse(result);
		
		return result;
	}
	
	public List<ShowlPropertyShapeGroup> relativePath(ShowlEffectiveNodeShape root) {
		List<ShowlPropertyShapeGroup> result = new ArrayList<>();
		for (ShowlPropertyShapeGroup p=this; p!=null; p=p.getDeclaringShape().getAccessor()) {
			result.add(p);
			if (p.getDeclaringShape() == root) {
				break;
			}
		}
		Collections.reverse(result);
		
		return result;
	}
	
	public String pathString() {
		List<ShowlPropertyShapeGroup> path = path();
		StringBuilder builder = new StringBuilder();
		builder.append(path.get(0).getDeclaringShape().toString());
		for (ShowlPropertyShapeGroup p : path) {
			builder.append('.');
			builder.append(p.getPredicate().getLocalName());
		}
		return builder.toString();
	}
	
	public String toString() {
		return pathString();
	}

	public ShowlEffectiveNodeShape getRootNode() {
		return declaringShape.getRoot();
	}

	public ShowlExpression getSelectedExpression() {
		return selectedExpression;
	}

	public void setSelectedExpression(ShowlExpression selectedExpression) {
		this.selectedExpression = selectedExpression;
	}

	
	/**
	 * Determine whether any member of this group is well-defined.
	 * @return True if any member of this group is well-defined.
	 */
	public boolean isWellDefined() {
		for (ShowlPropertyShape p : this) {
			if (ShowlUtil.isWellDefined(p)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the first ShowlPropertyShape within this group, or null if the group is empty.
	 */
	public ShowlPropertyShape first() {
		return isEmpty() ? null : get(0);
	}

	
	

}
