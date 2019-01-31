package io.konig.core.showl;

import java.util.ArrayList;

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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;

public class ShowlNodeShape implements Traversable {
	
	private ShowlPropertyShape accessor;
	private ShowlClass owlClass;
	private Shape shape; 
	
	private Map<URI,ShowlDirectPropertyShape> properties = new HashMap<>();
	private Map<URI, ShowlDerivedPropertyShape> derivedProperties = new HashMap<>();
	private Map<URI,ShowlInwardPropertyShape> inProperties = null;
	
	private List<ShowlJoinCondition> selectedJoins;
	
	public ShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {
		this.accessor = accessor;
		this.shape = shape;
		setOwlClass(owlClass);
		if (accessor != null) {
			accessor.setValueShape(this);
		}
	}
	
	public void addInwardProperty(ShowlInwardPropertyShape p) {
		if (inProperties == null) {
			inProperties = new HashMap<>();
		}
		inProperties.put(p.getPredicate(), p);
	}
	
	public ShowlInwardPropertyShape getInwardProperty(URI predicate) {
		return inProperties==null ? null : inProperties.get(predicate);
	}
	
	public Collection<ShowlInwardPropertyShape> getInwardProperties() {
		return inProperties==null ? Collections.emptyList() : inProperties.values();
	}
	
	public boolean isNamedRoot() {
		return accessor == null && findProperty(Konig.id) != null && hasDataSource();
	}
	
	
	
	public Collection<ShowlDirectPropertyShape> getProperties() {
		return properties.values();
	}
	
	public Set<ShowlPropertyShape> allOutwardProperties() {
		Set<ShowlPropertyShape> set = new HashSet<>();
		set.addAll(getProperties());
		set.addAll(getDerivedProperties());
		return set;
	}


	public void addProperty(ShowlDirectPropertyShape p) {
		properties.put(p.getPredicate(), p);
	}
	
	public ShowlPropertyShape getProperty(URI predicate) {
		return properties.get(predicate);
	}
	
	public ShowlDerivedPropertyShape getDerivedProperty(URI predicate) {
		return derivedProperties.get(predicate);
	}
	
	public Resource getId() {
		return shape.getId();
	}

	public Shape getShape() {
		return shape;
	}

	public ShowlClass getOwlClass() {
		return owlClass;
	}
	
	public boolean hasDataSource() {
		return shape != null && !shape.getShapeDataSource().isEmpty();
	}

	public void setOwlClass(ShowlClass owlClass) {
		if (this.owlClass != owlClass) {
			if (this.owlClass != null) {
				this.owlClass.getTargetClassOf().remove(this);
			}
			this.owlClass = owlClass;
			owlClass.addTargetClassOf(this);
		}
		
	}


	public boolean hasAncestor(Resource shapeId) {
		if (shape.getId().equals(shapeId)) {
			return true;
		}
		if (accessor != null) {
			return accessor.getDeclaringShape().hasAncestor(shapeId);
		}
		return false;
	}

	public ShowlPropertyShape getAccessor() {
		return accessor;
	}
	
	public void addDerivedProperty(ShowlDerivedPropertyShape p) {
		derivedProperties.put(p.getPredicate(), p);
	}
	
	
	
	/**
	 * Find an outward property.
	 */
	public ShowlPropertyShape findProperty(URI predicate) {
		ShowlPropertyShape p = getProperty(predicate);
		if (p == null) {
			p = getDerivedProperty(predicate);
		}
		return p;
	}

	public Collection<ShowlDerivedPropertyShape> getDerivedProperties() {
		return derivedProperties.values();
	}

	@Override
	public String getPath() {
		if (accessor == null) {
			return "{" + RdfUtil.localName(shape.getId()) + "}";
		}
		return accessor.getPath();
	}
	
	public String toString() {
		return getPath();
	}
	
	public ShowlNodeShape getRoot() {
		if (accessor == null) {
			return this;
		}
		return accessor.getDeclaringShape().getRoot();
	}
	
	public void addSelectedJoin(ShowlJoinCondition join) {
		if (selectedJoins==null) {
			selectedJoins = new ArrayList<>();
		}
		selectedJoins.add(join);
	}

	public List<ShowlJoinCondition> getSelectedJoins() {
		return selectedJoins == null ? Collections.emptyList() : selectedJoins;
	}


	
	

}
