package io.konig.shacl.impl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ClassAnalyzer {
	
	private static final Integer ZERO = new Integer(0);


	private ShapeManager shapeManager;
	private OwlReasoner owlReasoner;
	
	
	
	public ClassAnalyzer(ShapeManager shapeManager, OwlReasoner owlReasoner) {
		this.shapeManager = shapeManager;
		this.owlReasoner = owlReasoner;
	}
	
	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	/**
	 * Create a shape that aggregates all the known shapes for a given class.
	 * @param classId The id for the target class whose shapes are to be aggregated.
	 * @return The aggregated Shape.
	 */
	public Shape aggregate(Resource classId) {
		
		Map<String, Shape> map = new HashMap<>();
		
		return aggregate(map, classId);
	}
	
	public void pullDown(Shape shape) {
		AndConstraint and = shape.getAnd();
		if (and != null) {
			
			Set<Shape> memory = new HashSet<>();

			Map<URI, PropertyConstraint> map = mapShape(shape);
			Queue<Shape> queue = new LinkedList<>();
			for (Shape s : and.getShapes()) {
				queue.add(s);
			}
			
			while (!queue.isEmpty()) {
				Shape s = queue.remove();
				if (!memory.contains(s)) {
					memory.add(s);
					pullDown(s, shape, map);
					
					and = s.getAnd();
					if (and != null) {
						for (Shape t : and.getShapes()) {
							queue.add(t);
						}
					}
				}
			}
			shape.setAnd(null);
		}
		
		
	}
	
	private void pullDown(Shape s, Shape target, Map<URI, PropertyConstraint> map) {
		
		for (PropertyConstraint a : s.getProperty()) {
			URI predicate = a.getPredicate();
			if (predicate != null) {
				PropertyConstraint b = map.get(predicate);
				if (b == null) {
					PropertyConstraint c = new PropertyConstraint(predicate);
					c.setMinCount(a.getMinCount());
					c.setMaxCount(a.getMaxCount());
					c.setDatatype(a.getDatatype());
					c.setValueClass(valueClass(a));
					target.add(c);
					map.put(predicate, c);
				} else {

					mergeMinCount(b, a);
					mergeMaxCount(b, a);
					mergeDatatype(target, b, a);
					
					Resource valueClassA = valueClass(a);
					Resource valueClassB = valueClass(b);
					
					if (valueClassA != null) {
						if (valueClassB == null) {
							b.setValueClass(valueClassA);
						} else {
							if (owlReasoner.isSubClassOf(valueClassA, valueClassB)) {
								b.setValueClass(valueClassA);
							}
						}
					}
				}
			}
		}
		
	}

	private Map<URI, PropertyConstraint> mapShape(Shape shape) {
		Map<URI,PropertyConstraint> map = new HashMap<>();
		for (PropertyConstraint p : shape.getProperty()) {
			map.put(p.getPredicate(), p);
		}
		return map;
	}

	/**
	 * Remove any PropertyConstraints that duplicate constraints in the super class hierarchy.
	 * @param shape
	 */
	public void pushUp(Shape shape) {
		
		Set<Shape> memory = new HashSet<>();
		Map<URI,PropertyConstraint> map = new HashMap<>();
		addDerivedProperties(memory, map, shape);
		
		
		Iterator<PropertyConstraint> sequence = shape.getProperty().iterator();
		while (sequence.hasNext()) {
			PropertyConstraint b = sequence.next();
			PropertyConstraint a = map.get(b.getPredicate());
			
			if (a != null) {
				URI datatypeA = a.getDatatype();
				URI datatypeB = b.getDatatype();
				
				if (datatypeB != null && datatypeA!=null) {
					if (datatypeB.equals(datatypeA)) {
						sequence.remove();
						continue;
					}
					throw new KonigException("Incompatible datatype");
				}
				
				Resource valueClassA = valueClass(a);
				Resource valueClassB = valueClass(b);
				
				if (valueClassA!=null && valueClassB!=null) {
					if (owlReasoner.isSubClassOf(valueClassA, valueClassB)) {
						sequence.remove();
					}
				}
			}
			
			
		}
	}
	
	private void addDerivedProperties(Set<Shape> memory, Map<URI, PropertyConstraint> map, Shape shape) {
		
		if (!memory.contains(shape)) {
			memory.add(shape);

			AndConstraint and = shape.getAnd();
			if (and != null) {
				for (Shape s : and.getShapes()) {
					List<PropertyConstraint> plist = s.getProperty();
					for (PropertyConstraint p : plist) {
						URI predicate = p.getPredicate();
						if (!map.containsKey(predicate)) {
							map.put(predicate, p);
						}
					}
				}
				for (Shape s : and.getShapes()) {
					addDerivedProperties(memory, map, s);
				}
			}
		}
		
		
	}

	/**
	 * Merge the Or constraints into a PropertyConstraint list.
	 * @param shape The shape whose Or constraints are to be merged
	 */
	public void merge(Shape shape) {
		if (shape.getOr()!=null) {

			List<PropertyConstraint> sink = new ArrayList<>();
			shape.setPropertyList(sink);
			Map<URI,List<PropertyConstraint>> map = orPropertyMap(shape);
			shape.setOr(null);
			for (Entry<URI, List<PropertyConstraint>> e : map.entrySet()) {
				URI predicate = e.getKey();
				List<PropertyConstraint> list = e.getValue();
				
					
				PropertyConstraint a = null;
				
				for (PropertyConstraint b : list) {
					if (a == null) {
						a = new PropertyConstraint(predicate);
						a.setMinCount(minCount(b));
						a.setMaxCount(b.getMaxCount());
						a.setDatatype(b.getDatatype());
						a.setValueClass(valueClass(b));
						sink.add(a);
						
					} else {
						mergeMinCount(a, b);
						mergeMaxCount(a, b);
						mergeDatatype(shape, a, b);
						mergeValueClass(a, b);
					}
				}
			}
		}
			
	}
	
	private void mergeMaxCount(PropertyConstraint a, PropertyConstraint b) {

		Integer maxCountA = a.getMaxCount();
		Integer maxCountB = b.getMaxCount();
		
		if (maxCountB==null || (maxCountA!=null && maxCountB>maxCountA)) {
			a.setMaxCount(maxCountB);
		}
		
	}

	private void mergeMinCount(PropertyConstraint a, PropertyConstraint b) {

		Integer minCountA = a.getMinCount();
		Integer minCountB = minCount(b);
		
		if (minCountB > minCountA) {
			a.setMinCount(minCountB);
		}
		
	}

	private void mergeValueClass(PropertyConstraint a, PropertyConstraint b) {
		Resource valueClassA = a.getValueClass();
		Resource valueClassB = valueClass(b);
		
		Resource c = owlReasoner.leastCommonSuperClass(valueClassA, valueClassB);
		a.setValueClass(c);
	}

	private void mergeDatatype(Shape shape, PropertyConstraint a, PropertyConstraint b) {
		URI typeB = b.getDatatype();
		
		if (typeB !=null) {
			URI typeA = a.getDatatype();
			if (typeA!=null && !typeB.equals(typeA)) {
				throw new KonigException("Conflicting datatype: targetClass=" + 
						shape.getTargetClass() + ", predicate=" + a.getPredicate());
			}
			a.setDatatype(typeB);
		}
		
	}

	private Integer minCount(PropertyConstraint p) {
		Integer minCount = p.getMinCount();
		return minCount==null ? ZERO : minCount;
	}

	private Resource valueClass(PropertyConstraint a) {
		Resource valueClass = a.getValueClass();
		if (valueClass == null) {
			Shape valueShape = a.getShape(shapeManager);
			if (valueShape != null) {
				valueClass = valueShape.getTargetClass();
				if (valueClass == null && valueShape.getOr()!=null) {
					valueClass = mergeTargetClass(valueShape.getOr().getShapes());
				}
			}
		}
		return valueClass;
	}
	
	

	public Resource mergeTargetClass(List<Shape> shapes) {
		Resource result = null;
		for (Shape s : shapes) {
			URI targetClass = s.getTargetClass();
			if (result == null) {
				result = targetClass;
			} else {
				result = owlReasoner.leastCommonSuperClass(result, targetClass);
			}
		}
		return result;
	}

	private Map<URI, List<PropertyConstraint>> orPropertyMap(Shape shape) {
		Map<URI,List<PropertyConstraint>> map = new HashMap<>();
		for (Shape s : shape.getOr().getShapes()) {
			for (PropertyConstraint p : s.getProperty()) {
				URI predicate = p.getPredicate();
				List<PropertyConstraint> list = map.get(predicate);
				if (list == null) {
					list = new ArrayList<>();
					map.put(predicate, list);
				}
				list.add(p);
			}
		}
		return map;
	}

	private Shape aggregate(Map<String, Shape> map, Resource classId) {
		Shape result = map.get(classId.stringValue());
		if (result == null) {
			URI owlClass = (classId instanceof URI) ? (URI)classId : null;
			
			
			List<Shape> list = owlClass==null ? 
					new ArrayList<Shape>() : shapeManager.getShapesByTargetClass(owlClass);
					

			OrConstraint orList = new OrConstraint();
			
			result = new Shape();
			result.setTargetClass(owlClass);
			result.setOr(orList);
			
			for (Shape s : list) {
				orList.add(s);
			}
			
			
			buildHierarchy(map, result);
		}
		
		return result;
	}

	private void buildHierarchy(Map<String, Shape> map, Shape targetShape) {
		Resource id = targetShape.getTargetClass();
		String idValue = id.stringValue();
		if (!map.containsKey(idValue)) {
			
			map.put(idValue, targetShape);

			List<Vertex> superList = owlReasoner.getGraph().v(id).out(RDFS.SUBCLASSOF).toVertexList();
			AndConstraint andList = new AndConstraint();
			targetShape.setAnd(andList);
			
			for (Vertex superVertex : superList) {
				if (OWL.CLASS.equals(superVertex.getId())) {
					continue;
				}
				Shape superShape = aggregate(map, superVertex.getId());
				merge(superShape);
				andList.add(superShape);
			}
		}
	}

}
