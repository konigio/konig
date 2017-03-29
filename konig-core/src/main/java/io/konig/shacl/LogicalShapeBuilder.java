package io.konig.shacl;

/*
 * #%L
 * konig-shacl
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.AmbiguousPreferredClassException;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;

public class LogicalShapeBuilder {
	
	private OwlReasoner reasoner;
	private LogicalShapeNamer shapeNamer;
	private Set<URI> stack;
	private boolean usePropertyConstraintComment;
	
	
	public LogicalShapeBuilder(OwlReasoner reasoner, LogicalShapeNamer shapeNamer) {
		this.reasoner = reasoner;
		this.shapeNamer = shapeNamer;
	}

	public boolean isUsePropertyConstraintComment() {
		return usePropertyConstraintComment;
	}

	public void setUsePropertyConstraintComment(boolean usePropertyConstraintComment) {
		this.usePropertyConstraintComment = usePropertyConstraintComment;
	}

	private URI targetClass(Shape shape) {
		URI targetClass = shape.getTargetClass();
		try {
			return targetClass == null ? null : reasoner.preferredClassAsURI(targetClass);
		} catch (AmbiguousPreferredClassException e) {
			throw new RuntimeException(e);
		}
	}

	public void buildLogicalShapes(ShapeManager shapeManager, ClassManager classManager) {
		
		
		List<Shape> list = shapeManager.listShapes();
		
		stack = new HashSet<>();
		for (Shape shape : list) {
			URI targetClass = targetClass(shape);
			
			Shape logicalShape = classManager.getLogicalShape(targetClass);
			if (logicalShape == null) {
				buildLogicalShape(list, targetClass, classManager);
			}
		}
		
		List<Vertex> classList = reasoner.owlClassList();
		for (Vertex v : classList) {
			Resource id = v.getId();
			if (id instanceof URI) {
				URI uri = (URI) id;
				Shape logicalShape = classManager.getLogicalShape(uri);
				if (logicalShape == null) {
					buildLogicalShape(null, uri, classManager);
				}
			}
		}

		for (URI owlClass : stack) {
			
			Shape prior = classManager.getLogicalShape(owlClass);
			if (prior == null) {
				URI shapeId = shapeNamer.logicalShapeForOwlClass(owlClass);
				Shape shape = new Shape(shapeId);
				shape.setTargetClass(owlClass);
				classManager.addLogicalShape(shape);
			}
		}
		stack = null;
		
	}
	



	private void buildLogicalShape(List<Shape> list, URI targetClass, ClassManager classManager) {
		
		if (targetClass == null) {
			return;
		}
		
		URI shapeId = shapeNamer.logicalShapeForOwlClass(targetClass);
		
		Shape shape = new Shape(shapeId);
		shape.setTargetClass(targetClass);
		classManager.addLogicalShape(shape);
		
		List<Shape> filter = filterByTargetClass(list, targetClass);
		
		Map<String, List<PropertyConstraint>> map = buildPropertyMap(filter);
		for (List<PropertyConstraint> pList : map.values()) {
			mergeProperties(shape, pList);
		}
		
	}
	
	private Resource preferredClass(Resource owlClass) {
		try {
			return owlClass==null ? null : reasoner.preferredClass(owlClass).getId();
		} catch (AmbiguousPreferredClassException e) {
			throw new RuntimeException(e);
		}
	}

	private void mergeProperties(Shape shape, List<PropertyConstraint> pList) {
		
		PropertyConstraint constraint = null;
		
		for (PropertyConstraint p : pList) {
			if (constraint == null) {
				constraint = new PropertyConstraint(p.getPredicate());
				setDefaultComment(constraint);
				shape.add(constraint);
			}
			
			Integer minCountA = constraint.getMinCount();
			Integer minCountB = p.getMinCount();
			if (minCountA == null) {
				constraint.setMinCount(minCountB);
			} else if (minCountB == null || (minCountB < minCountA) ) {
				constraint.setMinCount(minCountB);
			}
			
			mergeComment(constraint, p);
			
			Integer maxCountA = constraint.getMaxCount();
			Integer maxCountB = p.getMaxCount();
			if (maxCountA==null) {
				constraint.setMaxCount(maxCountB);
			} else if (maxCountB==null || (maxCountB>maxCountA)) {
				constraint.setMaxCount(maxCountB);
			}
			
			Resource datatypeA = preferredClass(constraint.getDatatype());
			Resource datatypeB = preferredClass(p.getDatatype());
			
			Resource classA = preferredClass(constraint.getValueClass());
			Resource classB = preferredClass(p.getValueClass());
			Shape valueShapeA = constraint.getShape();
			Shape valueShapeB = p.getShape();
			
			if (valueShapeB!=null) {
				classB = leastUpperBound(shape, p, "sh:class", targetClass(valueShapeB), classB);
			}
			if (valueShapeA!=null && classA==null) {
				classA = valueShapeA.getTargetClass();
			}
			if (valueShapeA!=null && classA != null) {
				classA = leastUpperBound(shape, p, "sh:class", targetClass(valueShapeA), classA);
			}
			
			classB = leastUpperBound(shape, p, "sh:class", classB, classA);
			datatypeB = (URI) leastUpperBound(shape, p, "sh:datatype", datatypeB, datatypeA);

			if (classB instanceof URI) {
				stack.add((URI)classB);
			}
			
			constraint.setValueClass(classB);
			constraint.setDatatype((URI) datatypeB);
			
		}
		
	}





	private void mergeComment(PropertyConstraint a, PropertyConstraint b) {
		if (usePropertyConstraintComment && a.getComment()==null) {
			a.setComment(b.getComment());
		}
		
	}

	private void setDefaultComment(PropertyConstraint constraint) {
		URI predicate = constraint.getPredicate();
		Vertex v = reasoner.getGraph().getVertex(predicate);
		if (v != null) {
			String comment = RdfUtil.getDescription(v);
			if (comment != null) {
				constraint.setComment(comment);
			}
		}
		
	}

	private Resource leastUpperBound(Shape shape, PropertyConstraint p, String context, Resource classA, Resource classB) {
		if (classA!=null && classB==null) {
			return classA;
		}
		if (classB!=null && classA==null) {
			return classB;
		}
		if (classA != null && classB != null) {
			if (classA.equals(classB)) {
				return classA;
			}
			// TODO: compute least upper bound via semantic reasoning
			throw new RuntimeException("Conflicting " + context + " detected on property " + 
					p.getPredicate().getLocalName() + " of " +
					shape.getTargetClass().getLocalName());
		}
		return null;
	}



	private Map<String, List<PropertyConstraint>> buildPropertyMap(List<Shape> list) {
		
		Map<String,List<PropertyConstraint>> map = new HashMap<>();
		
		for (Shape shape : list) {
			List<PropertyConstraint> pList = shape.getProperty();
			for (PropertyConstraint p : pList) {
				URI predicate = p.getPredicate();
				
				List<PropertyConstraint> sink = map.get(predicate.stringValue());
				if (sink == null) {
					sink = new ArrayList<>();
					map.put(predicate.stringValue(), sink);
				}
				sink.add(p);
			}
		}
		
		
		return map;
	}



	private List<Shape> filterByTargetClass(List<Shape> list, URI targetClass) {
	
		List<Shape> result = new ArrayList<>();
		if (list != null) {
			for (Shape shape : list) {
				if (targetClass.equals(targetClass(shape))) {
					result.add(shape);
				}
			}
		}
		
		return result;
	}



}
