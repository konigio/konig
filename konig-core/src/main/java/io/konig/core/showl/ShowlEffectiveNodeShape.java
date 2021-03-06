package io.konig.core.showl;

import java.util.ArrayList;

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


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;

public class ShowlEffectiveNodeShape implements Comparable<ShowlEffectiveNodeShape> {
	
	private ShowlPropertyShapeGroup accessor;
	
	private Map<URI,ShowlPropertyShapeGroup> propertyMap = new HashMap<>();
	private int ranking;
	
	public static ShowlEffectiveNodeShape forNode(ShowlNodeShape node) {
		ShowlEffectiveNodeShape enode = new ShowlEffectiveNodeShape();
		addProperties(node, enode);
		return enode;
	}
	
	public static ShowlEffectiveNodeShape fromRoot(ShowlNodeShape node) {
		ShowlNodeShape root = node.getRoot();
		ShowlEffectiveNodeShape eroot = root.effectiveNode();
		if (node == root) {
			return eroot;
		}
		
		ShowlPropertyShapeGroup ep = eroot.findEffectiveProperty(node.getAccessor());
		return ep==null ? null : ep.getValueShape();
		
	}
	
	public boolean isAncestorOf(ShowlPropertyShapeGroup group) {
		Set<ShowlEffectiveNodeShape> memory = new HashSet<>();
		return isAncestor(memory, this, group);
	}

	private boolean isAncestor(Set<ShowlEffectiveNodeShape> memory, ShowlEffectiveNodeShape node,
			ShowlPropertyShapeGroup group) {
		ShowlEffectiveNodeShape parent = group.getDeclaringShape();
		if (parent == node) {
			return true;
		}
		if (!memory.contains(parent)) {
			memory.add(parent);
			ShowlPropertyShapeGroup accessor = node.getAccessor();
			if (accessor != null && isAncestor(memory, node, accessor)) {
				return true;
			}
			SynsetProperty synset = group.get(0).asSynsetProperty();
			URI predicate = group.getPredicate();
			for (ShowlPropertyShape p : synset) {
				if (!predicate.equals(p.getPredicate())) {
					ShowlPropertyShapeGroup otherGroup = p.asGroup();
					if (isAncestor(memory, node, otherGroup)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public ShowlNodeShape canonicalNode() {
		for (ShowlPropertyShapeGroup g : propertyMap.values()) {
			ShowlPropertyShape p = g.direct();
			if (p != null) {
				return p.getDeclaringShape();
			}
		}
		
		if (accessor != null) {
			ShowlPropertyShape p = accessor.direct();
			if (p != null && p.getValueShape()!=null) {
				return p.getValueShape();
			}
		}
		
		if (propertyMap.isEmpty()) {
			throw new IllegalStateException();
		}
		
		return propertyMap.values().iterator().next().iterator().next().getDeclaringShape();
	}
	

	public ShowlClass getTargetClass() {
		for (ShowlPropertyShapeGroup group : propertyMap.values()) {
			for (ShowlPropertyShape p : group) {
				ShowlNodeShape node = p.getDeclaringShape();
				ShowlClass targetClass = node.getOwlClass();
				if (targetClass != null) {
					return targetClass;
				}
			}
		}
		
		return null;
	}

	
	private ShowlPropertyShapeGroup findEffectiveProperty(ShowlPropertyShape p) {
		List<ShowlPropertyShape> list = p.propertyPath();
		ShowlPropertyShapeGroup ep = null;
		ShowlEffectiveNodeShape en = this;
		for (ShowlPropertyShape property : list) {
			if (en == null) {
				return null;
			}
			ep = en.findPropertyByPredicate(property.getPredicate());
			if (ep == null) {
				return null;
			}
			en = ep.getValueShape();
		}
		return ep;
	}

	private static void addProperties(ShowlNodeShape node, ShowlEffectiveNodeShape enode) {
		for (ShowlPropertyShape p : node.allOutwardProperties()) {
			URI predicate = p.getPredicate();
			ShowlPropertyShapeGroup ep = enode.findPropertyByPredicate(predicate);
			if (ep == null) {
				ep = new ShowlPropertyShapeGroup(enode, predicate);
				enode.addProperty(ep);
			}
			ep.add(p);
			if (p.getValueShape() != null) {
				ShowlEffectiveNodeShape valueShape = ep.getValueShape();
				if (valueShape == null) {
					valueShape = new ShowlEffectiveNodeShape();
					valueShape.setAccessor(ep);
					ep.setValueShape(valueShape);
				}
				addProperties(p.getValueShape(), valueShape);
			}
		}
		
	}

	protected ShowlEffectiveNodeShape() {
	}

	public ShowlPropertyShapeGroup getAccessor() {
		return accessor;
	}

	public void setAccessor(ShowlPropertyShapeGroup accessor) {
		this.accessor = accessor;
	}
	
	
	public Collection<ShowlPropertyShapeGroup> getProperties() {
		return propertyMap.values();
	}
	
	public void addProperty(ShowlPropertyShapeGroup p) {
		propertyMap.put(p.getPredicate(), p);
	}
	
	public ShowlPropertyShapeGroup findPropertyByPredicate(URI predicate) {
		return propertyMap.get(predicate);
	}
	
	public ShowlPropertyShapeSynSet findPropertyByPathWithSynonyms(List<ShowlPropertyShapeGroup> path) {
		
		ShowlPropertyShapeSynSet result = null;
		List<ShowlEffectiveNodeShape> nodeList = new ArrayList<>();
		nodeList.add(this);
		for (ShowlPropertyShapeGroup pathElement : path) {
			if (nodeList.isEmpty()) {
				return null;
			}
			result = pathElement.synonyms();
			
			
			
		}
		
		return null;
	}
	
	public ShowlPropertyShapeGroup findPropertyByPath(List<ShowlPropertyShapeGroup> path) {
		ShowlPropertyShapeGroup p = null;
		ShowlEffectiveNodeShape node = this;
		for (ShowlPropertyShapeGroup pathElement : path) {
			if (node == null) {
				return null;
			}
			p = node.findPropertyByPredicate(pathElement.getPredicate());
			if (p == null) {
				return null;
			}
			node = p.getValueShape();
		}
		
		return p;
	}
	
	public String toString() {
		ShowlNodeShape node = canonicalNode();
		if (node != null) {
			return node.getPath();
		}
		if (!propertyMap.isEmpty()) {
			ShowlPropertyShapeGroup p = propertyMap.values().iterator().next();
			if (!p.isEmpty()) {
				return p.get(0).getDeclaringShape().toString();
			}
		}
		
		return super.toString();
	}
	
	public ShowlEffectiveNodeShape getRoot() {
		return (accessor==null) ? null : accessor.getRootNode(); 
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public List<ShowlPropertyShapeGroup> path() {
		if (accessor == null) {
			return new ArrayList<>();
		}
		return accessor.path();
	}

	public ShowlPropertyShapeGroup findPropertyByPredicatePath(List<URI> path) {
		ShowlPropertyShapeGroup p = null;
		ShowlEffectiveNodeShape node = this;
		for (URI predicate : path) {
			if (node == null) {
				return null;
			}
			p = node.findPropertyByPredicate(predicate);
			if (p == null) {
				return null;
			}
			node = p.getValueShape();
		}
		
		return p;
	}

	public ShowlNodeShape directNode() {
		for (ShowlPropertyShapeGroup group : getProperties()) {
			ShowlPropertyShape direct = group.synonymDirect();
			if (direct != null) {
				return direct.getDeclaringShape();
			}
		}
		return null;
	}

	@Override
	public int compareTo(ShowlEffectiveNodeShape other) {
		ShowlNodeShape thisCanonical = canonicalNode();
		ShowlNodeShape otherCanonical = other.canonicalNode();
		
		URI thisId =  RdfUtil.uri(thisCanonical.getId());
		URI otherId = RdfUtil.uri(otherCanonical.getId());
		
		int result = thisId.getLocalName().compareTo(otherId.getLocalName());
		
		return result==0 ? thisId.getNamespace().compareTo(otherId.getNamespace()) : result;
	}


	
}
