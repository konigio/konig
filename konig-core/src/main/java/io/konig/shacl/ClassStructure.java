package io.konig.shacl;

/*
 * #%L
 * Konig Core
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.konig.core.*;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.core.util.SimpleValueMap;
import io.konig.core.util.ValueFormat;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.Schema;

/**
 * An entity that manages a collection of "logical" shapes for each known OWL class.
 * The logical shape contains the union of all property constraints from across the various
 * shapes of a given OWL class.
 * @author Greg McFall
 *
 */
public class ClassStructure {
	private static final Logger logger = LoggerFactory.getLogger(ClassStructure.class);
	private static final Integer ZERO = new Integer(0);
	private Map<Resource, Shape> shapeMap = new HashMap<>();
	private Map<URI,PropertyStructure> propertyMap = new HashMap<>();
	private Shape nullShape = new Shape(Konig.NullShape);
	private ValueFormat iriTemplate;
	private boolean failOnDatatypeConflict = false;
	private OwlReasoner reasoner;
	
	public ClassStructure() {
	}
	
	public ClassStructure(ValueFormat iriTemplate) {
		this.iriTemplate = iriTemplate;
	}
	
	public OwlReasoner getReasoner() {
		return reasoner;
	}

	public ClassStructure(ValueFormat iriTemplate, ShapeManager shapeManager, OwlReasoner reasoner) {
		this.iriTemplate = iriTemplate;
		init(shapeManager, reasoner);
	}
	
	public Collection<PropertyStructure> listProperties() {
		return propertyMap.values();
	}
	
	public PropertyStructure getProperty(URI propertyId) {
		return propertyMap.get(propertyId);
	}
	
	public Set<Resource> domainIncludes(URI propertyId) {
		PropertyStructure info = propertyMap.get(propertyId);
		if (info == null) {
			throw new KonigException("Property not found: " + propertyId);
		}
		Set<Resource> set = info.getDomainIncludes();
		if (set == null) {
			set = new HashSet<>();
			if (info.getDomain()!=null) {
				set.add(info.getDomain());
			}
		}
		return set;
	}
	
	public Collection<Shape> listClassShapes() {
		return shapeMap.values();
	}
	
	public Set<Resource> listClasses() {
		return shapeMap.keySet();
	}
	
	public void init(ShapeManager shapeManager, OwlReasoner reasoner) {
		if(reasoner != null && reasoner.getGraph() != null && reasoner.getGraph().getNamespaceManager() != null) {
			reasoner.getGraph().getNamespaceManager().add("owl", "http://www.w3.org/2002/07/owl#");
		}
		Builder builder = new Builder(shapeManager, reasoner);
		builder.build();
	}
	
	/**
	 * Get the "canonical" shape for a given OWL class.
	 * @param classId The id for the OWL class whose shape is to be returned.
	 * @return The shape with the following characteristics: (1) has a PropertyConstraint
	 * for each property that is locally declared (2) has an "OR" list of Shapes for all subclasses,
	 * (3) has an "AND" list of Shapes for all superclasses.
	 */
	public Shape getShapeForClass(Resource classId) throws OwlClassNotFoundException {
		Shape result = shapeForClass(classId);
		if (result == null) {
			throw new OwlClassNotFoundException(classId);
		}
		return result;
		
	}
	
	public Shape shapeForClass(Resource classId) {
		if (classId == null) {
			throw new IllegalArgumentException("classId cannot be null");
		}
		if (classId.equals(Schema.Thing)) {
			classId = OWL.THING;
		}
		Shape result = shapeMap.get(classId);
		return result;
	}
	
	public boolean isNullShape(Shape shape) {
		return Konig.NullShape.equals(shape.getId());
	}
	
	public boolean hasSubClass(Shape shape) {
		OrConstraint or = shape.getOr();
		return or!=null && !or.getShapes().isEmpty();
	}
	
	
	public Map<URI,PropertyConstraint> getPropertyMap(Shape shape) {
		Map<URI,PropertyConstraint> map = new HashMap<>();
		addProperties(map, shape);
		
		return map;
	}
	
	private void addProperties(Map<URI, PropertyConstraint> map, Shape shape) {
		for (PropertyConstraint p : shape.getProperty()) {
			URI predicate = p.getPredicate();
			if (predicate != null && !map.containsKey(predicate)) {
				map.put(predicate, p);
			}
		}
		List<Shape> superList = superClasses(shape);
		for (Shape s : superList) {
			addProperties(map, s);
		}
		
	}

	/**
	 * Get the properties declared by a given shape or any of its ancestors.
	 * @param shape The shape whose properties are to be returned.
	 */
	public List<PropertyConstraint> getProperties(Shape shape) {
//		if (!hasSuperClass(shape)) {
//			return shape.getProperty();
//		}
//		List<PropertyConstraint> list = new ArrayList<>();
//		LinkedList<Shape> stack = new LinkedList<>();
//		stack.add(shape);
//		while (!stack.isEmpty()) {
//			Shape s = stack.removeFirst();
//			for (PropertyConstraint p : s.getProperty()) {
//				if (!contains(list, p)) {
//					list.add(p);
//				}
//			}
//			stack.addAll(superClasses(s));
//		}
//		
//		return list;
		Map<URI,PropertyConstraint> map = getPropertyMap(shape);
		return new ArrayList<>(map.values());
	}

	public List<Shape> superClasses(Shape shape) {
		List<Shape> list = new ArrayList<>();
		addSuper(list, shape);
		return list;
	}
	
	private void addSuper(List<Shape> list, Shape shape) {
		if (hasSuperClass(shape)) {
			for (Shape s : shape.getAnd().getShapes()) {
				if (!contains(list, s)) {
					list.add(s);
				}
				addSuper(list, s);
			}
		}
	}
	
	/**
	 * Get the set consisting of a given shape plus all of its superclasses and subclasses.
	 */
	public List<Shape> transitiveClosure(Shape s) {
		List<Shape> list = superClasses(s);
		list.addAll(subClasses(s));
		list.add(s);
		return list;
	}
	

	public List<Shape> subClasses(Shape shape) {
		List<Shape> list = new ArrayList<>();
		addSubclassShapes(list, shape);
		return list;
	}
	
	

	private void addSubclassShapes(List<Shape> list, Shape shape) {
		OrConstraint or = shape.getOr();
		if (or != null) {
			for (Shape s : or.getShapes()) {
				if (!isNullShape(s) && !contains(list, s)) {
					list.add(s);
				}
				addSubclassShapes(list, s);
			}
		}
		
	}

	private boolean contains(List<Shape> list, Shape shape) {
		for (Shape s : list) {
			if (s == shape || shape.getId().equals(s.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Test whether a given class or one of its ancestors declares a specific property.
	 * @param owlClass The class to be tested
	 * @param property The specific property to be tested
	 * @return True if owlClass or one of its ancestors declares the property, and false otherwise.
	 */
	public boolean hasProperty(Resource owlClass, URI property) {
		Shape shape = getShapeForClass(owlClass);
		if (shape == null) {
			throw new KonigException("Class not found: " + owlClass);
		}
		return hasProperty(shape, property);
	}
	/**
	 * Test whether a given class or one of its ancestors declares a specific property.
	 * @param canonicalShape The canonical shape for the class to be tested
	 * @param property The specific property to be tested
	 * @return True if the canonical shape or one of its ancestors declares the property, and false otherwise.
	 */
	public boolean hasProperty(Shape canonicalShape, URI property) {
		if (canonicalShape.getPropertyConstraint(property)!=null) {
			return true;
		}
		AndConstraint and = canonicalShape.getAnd();
		if (and != null) {
			for (Shape ancestor : and.getShapes()) {
				if (hasProperty(ancestor, property)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Test whether some ancestor of a given class declares a specific property
	 * @param owlClass The given class to be tested.
	 * @param property The specific property to be tested
	 * @return True if some ancestor of the supplied owlClass declares the specified property, and false otherwise.
	 */
	public boolean ancestorHasProperty(Resource owlClass, URI property) throws OwlClassNotFoundException {
		Shape shape = getShapeForClass(owlClass);
		
		AndConstraint and = shape.getAnd();
		if (and != null) {
			for (Shape ancestor : and.getShapes()) {
				if (hasProperty(ancestor, property)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean hasSuperClass(Shape shape) {
		AndConstraint and = shape.getAnd();
		if (and != null) {
			List<Shape> list = and.getShapes();
			if (list.size()>1 || !OWL.THING.equals(list.get(0).getTargetClass())) {
				return true;
			}
		}
		return false;
	}
	
	
	private class Builder {
		private ShapeManager shapeManager;
		
		public Builder(ShapeManager shapeManager, OwlReasoner reasoner) {
			this.shapeManager = shapeManager;
			ClassStructure.this.reasoner = reasoner;
		}
		
		private void build() {
			scanShapes();
			scanProperties();
			scanClasses();
			buildShapes();
			buildHierarchy();
			injectThingAndNullShape();
			bubbleUp();
			bubbleDown();
		}
		
		

		private void bubbleDown() {
			
			for (Shape shape : listClassShapes()) {
				bubbleDown(shape);
			}
			
		}

		private void bubbleDown(Shape shape) {
			
			for (PropertyConstraint p : shape.getProperty()) {
				bubbleDown(shape, p);
			}
			
		}

		private void bubbleUp() {
			
			for (Shape shape : listClassShapes()) {
				bubbleUp(shape);
			}
			
		}

		private void bubbleUp(Shape shape) {
			
			for (PropertyConstraint p : shape.getProperty()) {
				bubbleUp(shape, p);
			}
			
		}

		private void bubbleDown(Shape shape, PropertyConstraint p) {
			URI predicate = p.getPredicate();
			Integer maxCount = p.getMaxCount();
			if (maxCount != null) {
				List<Shape> list = superClasses(shape);
				for (Shape superclass : list) {
					PropertyConstraint q = superclass.getPropertyConstraint(predicate);
					
					if (q!=null && q.getMaxCount() == null) {
						p.setMaxCount(null);
						break;
					}
				}
			}
			
		}

		private void bubbleUp(Shape shape, PropertyConstraint p) {

			URI predicate = p.getPredicate();
			Integer maxCount = p.getMaxCount();
			if (maxCount != null) {
				List<Shape> list = subClasses(shape);
				for (Shape subClass : list) {
					PropertyConstraint q = subClass.getPropertyConstraint(predicate);
					
					if (q!=null && q.getMaxCount() == null) {
						p.setMaxCount(null);
						break;
					}
				}
			}
		}

		private void scanShapes() {
			Graph graph = reasoner.getGraph();
			for (Shape shape : shapeManager.listShapes()) {
				URI targetClass = shape.getTargetClass();
				if (targetClass != null) {
					graph.edge(targetClass, RDF.TYPE, OWL.CLASS);
					Shape classShape = produceShape(targetClass);
					addProperties(shape, classShape);
				}
			}
			
		}

		private void addProperties(Shape shape, Shape classShape) {
			for (PropertyConstraint source : shape.getProperty()) {
				applyGlobalProperty(shape, source);
				URI predicate = source.getPredicate();
				PropertyConstraint target = classShape.getPropertyConstraint(predicate);
				if (target == null) {
					PropertyConstraint p = source.clone();
					Shape valueShape = p.getShape();
					if (valueShape != null) {
						Resource valueClass = p.getValueClass();
						if (valueClass == null) {
							p.setValueClass(valueShape.getTargetClass());
						}
						p.setShape(null);
					}
		
					classShape.add(p);
				} else {
					merge(source, target);
				}
			}
			
		}

		private void applyGlobalProperty(Shape shape, PropertyConstraint p) {
			URI targetClass = shape.getTargetClass();
			URI predicate = p.getPredicate();
			if (predicate != null) {
				PropertyStructure info = produceProperty(predicate);
				info.addShape(shape);
				setDomain(info, targetClass);
				setDatatype(info, p.getDatatype());
				setMaxCount(info, p.getMaxCount());
				setMinCount(info, p.getMinCount());
				setEquivalentPath(info, p.getEquivalentPath());
				setValueClass(info, p.getValueClass());
				if (p.getShape() != null) {
					setValueClass(info, p.getShape().getTargetClass());
				}
			}
			if (p.getValueClass() instanceof URI) {
				reasoner.getGraph().edge(p.getValueClass(), RDF.TYPE, OWL.CLASS);
			}
			
		}

		private void merge(PropertyConstraint source, PropertyConstraint target) {
			setDatatype(source, target);
			setEquivalentPath(source, target);
			setMaxCount(source, target);
			setMinCount(source, target);
			setValueClass(source, target);
		}

		private void setValueClass(PropertyConstraint source, PropertyConstraint p) {
			Resource value = source.getValueClass();
			if (value == null) {
				Shape shape = source.getShape();
				if (shape != null) {
					value = shape.getTargetClass();
				}
			}
			
			if (value != null) {
				if (p.getDatatype() != null) {
					p.setDatatype(null);
					p.setValueClass(OWL.THING);
					return;
				}
				Resource valueClass = p.getValueClass();
				if (valueClass == null) {
					p.setValueClass(value);
				} else {
					Resource result = reasoner.leastCommonSuperClass(valueClass, value);
					p.setValueClass(result);
				}
			}
			
		}

		private void setMinCount(PropertyConstraint source, PropertyConstraint target) {
			Integer a = minCount(source);
			Integer b = minCount(target);
			
			target.setMinCount(a<b ? a : b);
			
		}

		private Integer minCount(PropertyConstraint source) {
			Integer result = source.getMinCount();
			if (result == null) {
				result = ZERO;
			}
			return result;
		}

		private void setMaxCount(PropertyConstraint source, PropertyConstraint target) {
			Integer a = source.getMaxCount();
			Integer b = target.getMaxCount();
			if (a == null || b==null) {
				target.setMaxCount(null);
			} else if (a != null && (b==null || a>b)) {
				target.setMaxCount(a);
			}
			
		}

		private void setEquivalentPath(PropertyConstraint source, PropertyConstraint target) {
		
			if (source.getEquivalentPath() != null) {
				target.setEquivalentPath(source.getEquivalentPath());
			}
			
		}

		private void setDatatype(PropertyConstraint source, PropertyConstraint p) {
			URI value = source.getDatatype();
			if (value != null) {
				
				if (p.getPredicate().equals(RDF.TYPE)) {
					p.setDatatype(null);
					p.setValueClass(OWL.CLASS);
					return;
				}
				if (p.getValueClass()!=null) {
					p.setDatatype(null);
					p.setValueClass(OWL.THING);
				}
				Resource prior = p.getDatatype();
				if (prior!=null && !prior.equals(value)) {
					if (failOnDatatypeConflict) {
						throw new KonigException("Conflicting datatype on property <" + p.getPredicate() + ">: Found <" +
							prior + "> and <" + value + ">"
						);
					} else {
						
						logger.warn("Conflicting datatype on property <{}>: Found <{}> and <{}>. Using rdfs:Literal", p.getPredicate().stringValue(), prior.stringValue(), value.stringValue());
						value = RDFS.LITERAL;
					}
				}
				
				p.setDatatype(value);
			}
		}
		

		private void subClassOf(Shape subtype, Shape supertype) {
			OrConstraint or = supertype.getOr();
			if (or == null) {
				or = new OrConstraint();
				supertype.setOr(or);
			}
			or.add(subtype);
			
			AndConstraint and = subtype.getAnd();
			if (and == null) {
				and = new AndConstraint();
				subtype.setAnd(and);
			}
			and.add(supertype);
		}
		
		private void injectThingAndNullShape() {
			
			Shape thing = produceShape(OWL.THING);
			
			for (Shape shape : shapeMap.values()) {
				if (shape != thing) {
					if (!hasSuperClass(shape)) {
						subClassOf(shape, thing);
					}
					OrConstraint or = shape.getOr();
					if (or != null) {
						if (or.getShapes().size()==1) {
							or.add(nullShape);
						}
					}
				}
			}
		}

		
		private void scanClasses() {
			List<Vertex> classList = reasoner.getGraph().v(OWL.CLASS).in(RDF.TYPE).toVertexList();
			for (Vertex v : classList) {
				produceShape(v.getId());
			}
		}

		private void buildHierarchy() {
			List<Shape> shapeList = new ArrayList<>(shapeMap.values());
			
			for (Shape shape : shapeList) {
				URI targetClass = shape.getTargetClass();
				if (targetClass != null) {
					Set<URI> subClasses = reasoner.subClasses(targetClass);
					if (!subClasses.isEmpty()) {
						OrConstraint orList = new OrConstraint();
						shape.setOr(orList);
						
						for (URI subclassId : subClasses) {
							Shape s = produceShape(subclassId);
							orList.add(s);
						}
					}
					Set<URI> superClasses = reasoner.superClasses(targetClass);
					if (!superClasses.isEmpty()) {
						AndConstraint andList = new AndConstraint();
						shape.setAnd(andList);
						for (URI superclassId : superClasses) {
							andList.add(produceShape(superclassId));
						}
					}
				}
			}
		}

		private void buildShapes() {
			for (PropertyStructure info : propertyMap.values()) {
				Resource domain = info.getDomain();
				URI predicate = info.getPredicate();
				if (domain != null) {
					Shape shape = produceShape(domain);
					if (shape.getPropertyConstraint(predicate) == null) {
						shape.add(info.asPropertyConstraint());
					}
				}
				Set<Resource> domainIncludes = info.getDomainIncludes();
				if (domainIncludes != null) {
					for (Resource owlClass : domainIncludes) {
						Shape shape = produceShape(owlClass);
						if (shape.getPropertyConstraint(predicate) == null) {
							shape.add(info.asPropertyConstraint());
						}
					}
				}
			}
			
		}

		private void scanProperties() {
			List<Vertex> propertyList = reasoner.getGraph().v(RDF.PROPERTY).union(
				OWL.DATATYPEPROPERTY, OWL.OBJECTPROPERTY, OWL.FUNCTIONALPROPERTY, OWL.INVERSEFUNCTIONALPROPERTY, OWL.SYMMETRICPROPERTY, 
				OWL.TRANSITIVEPROPERTY, OWL.ANNOTATIONPROPERTY, OWL.DEPRECATEDPROPERTY, OWL.ONTOLOGYPROPERTY, OwlVocab.ReflexiveProperty, 
				OwlVocab.IrreflexiveProperty, OwlVocab.AsymetricProperty).in(RDF.TYPE).distinct().toVertexList();
			
			for (Vertex v : propertyList) {
				if (v.getId() instanceof URI) {
					URI propertyId = (URI) v.getId();

					boolean isFunctional = reasoner.instanceOf(propertyId, OWL.FUNCTIONALPROPERTY);
					URI domain = v.getURI(RDFS.DOMAIN);
					URI range = v.getURI(RDFS.RANGE);
					String comment = RdfUtil.getDescription(v);
					PropertyStructure p = produceProperty(propertyId);
					p.setDescription(comment);
					if (domain != null) {
						p.setDomain(domain);
						p.setDomainLocked(true);
					} else {
						Set<URI> set = v.asTraversal().out(Schema.domainIncludes).toUriSet();
						if (!set.isEmpty()) {
							for (URI uri : set) {
								p.domainIncludes(uri);
							}
							p.setDomainIncludesLocked(true);
						}
					}
					if (isFunctional) {
						setMaxCount(p, 1);
					}
					if (range != null) {
						if (reasoner.isSubclassOfLiteral(range)) {
							setDatatype(p, range);
						} else {
							setValueClass(p, range);
						}
					}
					
					
				}
				
			}
		}

		private void setDomain(PropertyStructure p, Resource value) {
			if (value != null) {
				if (Schema.Thing.equals(value)) {
					value = OWL.THING;
				}
				Resource prior = p.getDomain();
				
				if (!value.equals(prior)) {

					if(p.getDomainIncludes() != null) {
						
						Iterator<Resource> sequence = p.getDomainIncludes().iterator();
						while (sequence.hasNext()) {
							Resource other = sequence.next();
							if (other.equals(value)) {
								return;
							}
							
							if (p.isDomainIncludesLocked()) {
								
								if (reasoner.isSubClassOf(value, other)) {
									return;
								}
								
								if (reasoner.isSubClassOf(other, value)) {
									sequence.remove();
								}
								
							} else {
								Resource common = reasoner.leastCommonSuperClass(value, other);
								if (common.equals(other)) {
									return;
								}
								if (!common.equals(OWL.THING)) {
									sequence.remove();
									value = common;
								}
							}
							
							
						}
						p.domainIncludes(value);
						
					} else if (prior == null) {
						p.setDomain(value);
					} else {
						
						if (OWL.THING.equals(prior)) {
							p.domainIncludes(value);
						} else if (OWL.THING.equals(value)) {
							p.domainIncludes(prior);
						} else {
							Resource common = reasoner.leastCommonSuperClass(prior, value);
							if (OWL.THING.equals(common)) {
								p.domainIncludes(prior);
								p.domainIncludes(value);
								if (!p.isDomainLocked()) {
									p.setDomain(null);
								}
							} else if (p.isDomainLocked()) {
								p.domainIncludes(common);
							} else {
								p.setDomain(common);
							}
						}
					}
				}
				
			}
			
		}

		private void setValueClass(PropertyStructure p, Resource value) {
			if (value != null) {
				if (p.getDatatype() != null) {
					throw new KonigException("Property <" + p.getPredicate() + "> has sh:datatype <" + p.getDatatype() + "> and sh:class <" +
							value + ">"
					);
				}
				Resource valueClass = p.getValueClass();
				if (valueClass == null) {
					p.setValueClass(value);
				} else {
					Resource result = reasoner.leastCommonSuperClass(valueClass, value);
					p.setValueClass(result);
				}
			}
			
		}

		private void setDatatype(PropertyStructure p, URI value) {
			if (value != null) {
				if (p.getPredicate().equals(RDF.TYPE)) {
					p.setDatatype(null);
					p.setValueClass(OWL.CLASS);
					return;
				}
				if (p.getValueClass() != null) {
					throw new KonigException("Property <" + p.getPredicate() + "> has sh:datatype <" + value + "> and sh:class <" +
							p.getValueClass() + ">"
					);
				}
				Resource prior = p.getDatatype();
				if (prior!=null && !prior.equals(value)) {
					if (failOnDatatypeConflict) {
						throw new KonigException("Conflicting datatype on property <" + p.getPredicate() + ">: Found <" +
							prior + "> and <" + value + ">"
						);
					} else {
						
						logger.warn("Conflicting datatype on property <{}>: Found <{}> and <{}>. Using rdfs:Literal", p.getPredicate().stringValue(), prior.stringValue(), value.stringValue());
						value = RDFS.LITERAL;
					}
				}
				
				p.setDatatype(value);
			}
			
		}
		
		private void setEquivalentPath(PropertyStructure p, Path path) {
			if (path != null) {
				p.setEquivalentPath(path);
			}
		}

		private void setMaxCount(PropertyStructure p, Integer value) {
			if (value != null) {
				Integer prior = p.getMaxCount();
				if (prior==null || value>prior) {
					p.setMaxCount(value);
				}
			}
		}



		private void setMinCount(PropertyStructure info, Integer value) {
			if (value != null) {
				Integer prior = info.getMinCount();
				if (prior==null || value<prior) {
					info.setMinCount(value);
				}
			}
			
		}

		private PropertyStructure produceProperty(URI predicate) {
			PropertyStructure p = propertyMap.get(predicate);
			if (p == null) {
				p = new PropertyStructure(predicate);
				propertyMap.put(predicate, p);
			}
			return p;
		}

		private Shape produceShape(Resource targetClassId) {
			if (targetClassId.equals(Schema.Thing)) {
				targetClassId = OWL.THING;
			}
			Shape shape = shapeMap.get(targetClassId);
			if (shape == null) {
				URI targetClassURI = (targetClassId instanceof URI) ? (URI) targetClassId : null;
				URI shapeId = (iriTemplate==null || targetClassURI==null) ? null : shapeName(targetClassURI);
				shape = new Shape(shapeId);
				if (targetClassId instanceof URI) {
					shape.setTargetClass((URI) targetClassId);
				} 
				shapeMap.put(targetClassId, shape);
				PropertyConstraint p = new PropertyConstraint(RDF.TYPE);
				p.setNodeKind(NodeKind.IRI);
				p.setValueClass(OWL.CLASS);
				shape.add(p);
			}
			return shape;
		}
		
		private URI shapeName(URI targetClassURI) {
			SimpleValueMap map = new SimpleValueMap();
			map.put("targetClassLocalName", targetClassURI.getLocalName());
//			reasoner.getGraph().getNamespaceManager().add("owl", "http://www.w3.org/2002/07/owl#");
			Namespace ns = reasoner.getGraph().getNamespaceManager().findByName(targetClassURI.getNamespace());
			if (ns == null) {
				throw new KonigException("Prefix not found for namespace " + targetClassURI.getNamespace());
			}
			map.put("targetClassNamespacePrefix", ns.getPrefix());
			String value = iriTemplate.format(map);
			
			return new URIImpl(value);
		}
		
	}
	

	public List<URI> listSubclasses(Shape shape) {
		List<URI> list = new ArrayList<>();
		addSubclasses(list, shape);
		return list;
	}

	private void addSubclasses(List<URI> list, Shape shape) {
		if (shape.getOr() != null) {
			for (Shape s : shape.getOr().getShapes()) {
				if (nullShape == s) {
					continue;
				}
				URI targetClass = s.getTargetClass();
				list.add(targetClass);
				addSubclasses(list, s);
			}
		}
	}

	public Shape getNullShape() {
		return nullShape;
	}

}
