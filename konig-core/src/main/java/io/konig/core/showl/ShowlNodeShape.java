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
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;

/**
 * A particular instance of a SHACL NodeShape.
 * The instance may be bound to a particular location with a graph via the accessor, and it may
 * be bound to a particular DataSource.
 * 
 * @author Greg McFall
 *
 */
public class ShowlNodeShape implements Traversable {
	
	private ShowlPropertyShape accessor;
	private ShowlClass owlClass;
	private Shape shape; 
	
	private Map<URI,ShowlDirectPropertyShape> properties = new HashMap<>();
	private Map<URI, ShowlDerivedPropertyList> derivedProperties = null;
	private Map<URI,ShowlInwardPropertyShape> inProperties = null;
	
	private List<ShowlJoinCondition> selectedJoins;
	private ShowlDataSource shapeDataSource;
	private boolean unmapped;
	
	private List<ShowlChannel> channelList;
	private ShowlPropertyShape targetProperty;
	
	
	public ShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {
		derivedProperties = new HashMap<>();
		this.accessor = accessor;
		this.shape = shape;
		setOwlClass(owlClass);
		if (accessor != null) {
			accessor.setValueShape(this);
		}
	}
	
	/**
	 * Get the key through which the specified source node is joined according to the given join condition.
	 * @param sourceNode The source node whose key is being requested
	 * @param join The join condition for the key.
	 */
	public ShowlPropertyShape keyProperty(ShowlNodeShape sourceNode, ShowlJoinCondition join) {
		if (join instanceof ShowlFromCondition) {
			for (ShowlJoinCondition j : getSelectedJoins()) {
				if (j == join) {
					continue;
				}
				
				ShowlNodeShape otherNode = j.otherNode(sourceNode);
				if (otherNode != null) {
					return j.propertyOf(sourceNode);
				}
			}
		} else {
			return join.propertyOf(sourceNode);
		}
		
		return null;
	}
	
	/**
	 * Get the properties of some otherNode that are reachable from the selected mappings
	 * of this NodeShape.
	 */
	public Set<ShowlPropertyShape> joinProperties(ShowlNodeShape otherNode) throws ShowlProcessingException {

		Set<ShowlPropertyShape> set = new HashSet<>();
		
		for (ShowlDirectPropertyShape p : getProperties()) {
			
			if (p.getValueShape() != null) {
				
				Set<ShowlPropertyShape> nested = p.getValueShape().joinProperties(otherNode);
				set.addAll(nested);
				continue;
			}
			
			ShowlMapping mapping = p.getSelectedMapping();
			if (mapping == null) {
				throw new ShowlProcessingException("Mapping not found for " + p.getPath());
			}
			
			ShowlPropertyShape otherProperty = mapping.findOther(p);
			
				
			if (otherNode == otherProperty.getDeclaringShape()) {
				addMappedProperty(set, otherProperty, p);
			}
			
		}
		
		return set;
	}
	
	/**
	 * Get all the properties mapped to this NodeShape via a given join condition.
	 */
	public Set<ShowlPropertyShape> joinProperties(ShowlJoinCondition join) throws ShowlProcessingException {
		
		Set<ShowlPropertyShape> set = new HashSet<>();
		
		if (join instanceof ShowlFromCondition) {
			ShowlFromCondition from = (ShowlFromCondition) join;
			join = from.getDerivedFrom();
		}

		for (ShowlDirectPropertyShape p : getProperties()) {
			
			ShowlMapping mapping = p.getSelectedMapping();
			if (mapping == null) {
				throw new ShowlProcessingException("Mapping not found for " + p.getPath());
			}
			
			if (mapping.getJoinCondition() == join) {
				ShowlPropertyShape other = mapping.findOther(p);
				addMappedProperty(set, other, p);
				
			}
		}
		
		return set;
		
	}
	
	private void addMappedProperty(Set<ShowlPropertyShape> set, ShowlPropertyShape other, ShowlPropertyShape target) {

		
		
		if ((other instanceof ShowlDirectPropertyShape) || 
			(other instanceof ShowlStaticPropertyShape) ||
			(other.getValueShape() != null)
		) {
			set.add(other);
			return;
			
		} else {
			ShowlPropertyShape peer = other.getPeer();
			if (peer instanceof ShowlDirectPropertyShape) {
				set.add(other);
				return;
			}
			
			if (other instanceof ShowlTemplatePropertyShape) {
				addTemplateParameters(set, (ShowlTemplatePropertyShape) other, target);
				return;
			}
		}
		
		throw new ShowlProcessingException("Mapping not supported for " + target.getPath());
		
	}
	
	public ShowlPropertyShape enumSourceKey(OwlReasoner reasoner) {
		if (accessor != null && reasoner.isEnumerationClass(owlClass.getId())) {
			ShowlMapping mapping = accessor.getSelectedMapping();
			if (mapping != null) {
				ShowlPropertyShape otherAccessor = mapping.findOther(accessor);
				ShowlNodeShape sourceNode = otherAccessor.getValueShape();
				if (sourceNode != null) {
					for (ShowlPropertyShape p : sourceNode.allOutwardProperties()) {
						if (reasoner.isInverseFunctionalProperty(p.getPredicate())) {
							if (p.isDirect()) {
								return p;
							}
							
							ShowlPropertyShape peer = p.getPeer();
							if (peer instanceof ShowlDirectPropertyShape) {
								return peer;
							}
						}
						
						
					}
				}
			}
		}
		return null;
	}

	private void addTemplateParameters(Set<ShowlPropertyShape> set, ShowlTemplatePropertyShape other, ShowlPropertyShape target) {
		
		IriTemplate template = other.getTemplate();
		Context context = template.getContext();
		
		ShowlNodeShape node = other.getDeclaringShape();
		
		for (Element e : template.toList()) {
			if (e.getType() == ElementType.VARIABLE) {
				URI varId = new URIImpl(context.expandIRI(e.getText()));
				
				List<ShowlPropertyShape> outSet = node.out(varId);
				
				for (ShowlPropertyShape p : outSet) {
					addMappedProperty(set, p, target);
				}
				
			}
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
	
	@Deprecated
	public boolean isNamedRoot() {
		return accessor == null && findProperty(Konig.id) != null && hasDataSource();
	}
	
	
	
	public Collection<ShowlDirectPropertyShape> getProperties() {
		return properties.values();
	}
	
	public Set<ShowlPropertyShape> allOutwardProperties() {
		Set<ShowlPropertyShape> set = new HashSet<>();
		set.addAll(getProperties());
		for (List<ShowlDerivedPropertyShape> list : getDerivedProperties()) {
			set.addAll(list);
		}
		return set;
	}


	public void addProperty(ShowlDirectPropertyShape p) {
		properties.put(p.getPredicate(), p);
	}
	
	public ShowlDirectPropertyShape getProperty(URI predicate) {
		return properties.get(predicate);
	}
	
	/**
	 * Get the list of derived properties having a specified predicate.
	 * This is a list because there are multiple ways in which a given property might be derived:
	 * <ol>
	 *   <li> Explicit derived property having a formula within the sh:NodeShape
	 *   <li> Implicit property from some other formula that includes the predicate in a path expression.
	 * </ol>
	 * 
	 * In the case where the property is induced from a path expression, a separate entry is required
	 * for each path expression that has a filter.
	 */
	public ShowlDerivedPropertyList getDerivedProperty(URI predicate) {
		ShowlDerivedPropertyList list = derivedProperties.get(predicate);
		return list == null ? new ShowlDerivedPropertyList(predicate) : list;
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
			if (owlClass != null) {
				owlClass.addTargetClassOf(this);
			}
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
		ShowlDerivedPropertyList list = derivedProperties.get(p.getPredicate());
		if (list == null) {
			list = new ShowlDerivedPropertyList(p.getPredicate());
			derivedProperties.put(p.getPredicate(),  list);
		}
		list.add(p);
	}
	
	public ShowlPropertyShape findOut(URI predicate) {
		ShowlPropertyShape p = properties.get(predicate);
		if (p != null) {
			return p;
		}
		ShowlDerivedPropertyList indirect = derivedProperties.get(predicate);
		if (indirect != null) {
			for (ShowlDerivedPropertyShape derived : indirect) {
				// TODO: Do we need special handling for filtered properties?
				return derived;
			}
		}
		
		return null;
	}
	
	public List<ShowlPropertyShape> out(URI predicate) {
		ShowlDirectPropertyShape direct = properties.get(predicate);
		ShowlDerivedPropertyList indirect = derivedProperties.get(predicate);
		
		if (direct==null && indirect==null) {
			return Collections.emptyList();
		}
		
		List<ShowlPropertyShape> list = new ArrayList<>();
		if (direct != null) {
			list.add(direct);
		}
		if (indirect!=null) {
			list.addAll(indirect);
		}
		return list;
	}
	
	
	/**
	 * Find an outward property.
	 */
	@Deprecated
	public ShowlPropertyShape findProperty(URI predicate) {
		ShowlPropertyShape p = getProperty(predicate);
		if (p == null) {
			List<ShowlDerivedPropertyShape> list = derivedProperties.get(predicate);
			if (list != null && list.size() == 1) {
				p = list.get(0);
			}
		}
		return p;
	}

	public Collection<ShowlDerivedPropertyList> getDerivedProperties() {
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

	/**
	 * Returns true if there are no shapes that are candidates as sources from which this node may 
	 * be derived.
	 */
	public boolean isUnmapped() {
		return unmapped;
	}


	public void setUnmapped(boolean unmapped) {
		this.unmapped = unmapped;
	}


	public NodeKind getNodeKind() {
		return shape == null ? null : shape.getNodeKind();
	}

	public ShowlStaticPropertyShape staticProperty(ShowlProperty property) {
		URI predicate = property.getPredicate();
		ShowlDerivedPropertyList list = derivedProperties.get(predicate);
		if (list != null) {
			for (ShowlDerivedPropertyShape p : list) {
				if (p instanceof ShowlStaticPropertyShape) {
					return (ShowlStaticPropertyShape) p;
				}
			}
		}
		if (list == null) {
			list = new ShowlDerivedPropertyList(predicate);
			derivedProperties.put(predicate, list);
		}
		
		ShowlStaticPropertyShape p = new ShowlStaticPropertyShape(this, property);
		list.add(p);
		return p;
	}

	
	public ShowlDataSource getShapeDataSource() {
		return shapeDataSource;
	}

	public void setShapeDataSource(ShowlDataSource shapeDataSource) {
		this.shapeDataSource = shapeDataSource;
	}
	
	public void addChannel(ShowlChannel channel) {
		if (channelList == null) {
			channelList = new ArrayList<>();
		}
		channelList.add(channel);
		
	}

	public List<ShowlChannel> getChannels() {
		return channelList == null ? Collections.emptyList() : channelList;
	}
	
	/**
	 * Get all properties from a specified source NodeShape that contribute to the 
	 * definition of this target NodeShape.  This includes selected properties from the
	 * source NodeShape, and properties in any join statements.
	 */
	public Set<ShowlPropertyShape> selectedPropertiesOf(ShowlNodeShape sourceNodeShape) {
		Set<ShowlPropertyShape> set = new HashSet<>();
		addSelectedProperties(set, sourceNodeShape);
		addJoinProperties(set, sourceNodeShape);
		return set;
	}

	private void addJoinProperties(Set<ShowlPropertyShape> set, ShowlNodeShape sourceNodeShape) {
		for (ShowlChannel channel : getChannels()) {
			ShowlStatement statement = channel.getJoinStatement();
			if (statement != null) {
				statement.addDeclaredProperties(sourceNodeShape, set);
			}
		}
		
	}

	private void addSelectedProperties(Set<ShowlPropertyShape> set, ShowlNodeShape sourceNodeShape) {
		for (ShowlDirectPropertyShape direct : getProperties()) {
			ShowlExpression e = direct.getSelectedExpression();
			if (e != null) {
				e.addDeclaredProperties(sourceNodeShape, set);
			}
			if (direct.getValueShape() != null) {
				addSelectedProperties(set, direct.getValueShape());
			}
		}
		
	}

	/**
	 * Get the property on the target NodeShape to which this
	 * source NodeShape is bound.
	 */
	public ShowlPropertyShape getTargetProperty() {
		return targetProperty;
	}

	public void setTargetProperty(ShowlPropertyShape targetProperty) {
		this.targetProperty = targetProperty;
	}
	
	
}
