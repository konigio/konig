package io.konig.schemagen.merge;

/*
 * #%L
 * Konig Schema Generator
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.OwlReasoner;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * A utility that merges property constraints from two shapes for a given
 * class into a single shape.  The least restrictive constraints are adopted in 
 * the merged shape.
 * @author Greg McFall
 *
 */
public class ShapeAggregator {
	
	private OwlReasoner owl;
	private ShapeManager shapeManager;
	
	public ShapeAggregator() {
		
	}
	
	
	public ShapeAggregator(OwlReasoner owl, ShapeManager shapeManager) {
		this.owl = owl;
		this.shapeManager = shapeManager;
	}


	public Shape merge(URI shapeName, Shape a, Shape b) throws SchemaGeneratorException {
		
		Shape shape = new Shape(shapeName);
		Map<String, PropertyConstraint> map = mapProperties(b);
		for (PropertyConstraint x : a.getProperty()) {
			String key = x.getPredicate().stringValue();
			PropertyConstraint y = map.get(key);
			
			if (y == null) {
				PropertyConstraint p = x.clone();
				p.setMinCount(0);
				shape.add(p);
			} else {
				shape.add(merge(shape, x, y));
				map.remove(key);
			}
		}
		
		for (PropertyConstraint c : map.values()) {
			shape.add(c.clone());
		}
		
		return shape;
	}



	private PropertyConstraint merge(Shape shape, PropertyConstraint x, PropertyConstraint y) {
		
		PropertyConstraint p = new PropertyConstraint(x.getPredicate());
		setAllowedValues(p, x, y);
		setMinCount(p, x, y);
		setMaxCount(p, x, y);
		setValueClass(p, x, y);
		setDatatype(p, x, y);
		setValueShape(shape, p, x, y);
		
		return p;
	}



	private void setValueShape(Shape parent, PropertyConstraint p, PropertyConstraint x, PropertyConstraint y) {
		
		Resource aId = x.getShapeId();
		Resource bId = y.getShapeId();
		
		if (aId !=null && bId==null) {
			setValueShape(p, aId, x.getShape());
		} else if (aId==null && bId!=null) {
			setValueShape(p, bId, y.getShape());
		} else if (aId!=null && bId!=null) {
			Shape c = valueShape(x);
			Shape d = valueShape(y);
			StringBuilder builder = new StringBuilder(parent.getId().stringValue());
			builder.append('.');
			builder.append(p.getPredicate().getLocalName());
			
			URI uri = new URIImpl(builder.toString());
			
			Shape valueShape = merge(uri, c, d);
			p.setShape(valueShape);
			shapeManager.addShape(valueShape);
			
		}
		
	}

	private Shape valueShape(PropertyConstraint x) {
		Shape s = x.getShape();
		if (s == null) {
			if (shapeManager == null) {
				throw new SchemaGeneratorException("shapeManager is not defined");
			}
			
			Resource id = x.getShapeId();
			if (id instanceof URI) {
				s = shapeManager.getShapeById((URI)id);
				if (s == null) {
					throw new SchemaGeneratorException("Shape not found: " + id.stringValue());
				}
			} else {
				throw new SchemaGeneratorException("Blank node not supported for value shapes");
			}
			
		}
		return s;
	}


	private void setValueShape(PropertyConstraint p, Resource id, Shape shape) {
		
		if (shape == null) {
			shape = shapeManager.getShapeById(id);
			if (shape == null) {
				throw new SchemaGeneratorException("Shape not found: " + id);
			}
		}

		p.setShape(shape);
		
	}


	private void setDatatype(PropertyConstraint p, PropertyConstraint x, PropertyConstraint y) {
		URI a = x.getDatatype();
		URI b = y.getDatatype();
		
		if (a != null && b!=null) {
			
			if (a.equals(b)) {
				p.setDatatype(a);
			} else {
				p.setValueClass(owl.leastCommonSuperDatatype(a, b));
			}
		}
		
	}

	private void setValueClass(PropertyConstraint p, PropertyConstraint x, PropertyConstraint y) {
		
		Resource a = x.getValueClass();
		Resource b = y.getValueClass();
		
		if (a != null && b!=null) {
			p.setValueClass(owl.leastCommonSuperClass(a, b));
		}
		
	}

	private void setMaxCount(PropertyConstraint p, PropertyConstraint x, PropertyConstraint y) {
		Integer a = x.getMaxCount();
		Integer b = y.getMaxCount();
		
		if (a!=null && b!=null) {
			Integer max = Math.max(a, b);
			p.setMaxCount(max);
		}
		
	}

	private void setMinCount(PropertyConstraint p, PropertyConstraint x, PropertyConstraint y) {
		Integer a = x.getMinCount();
		Integer b = y.getMinCount();
		
		if (a!=null && b!=null) {
			Integer min = Math.min(a, b);
			p.setMinCount(min);
		} else {
			p.setMinCount(0);
		}
		
	}

	private void setAllowedValues(PropertyConstraint p, PropertyConstraint x, PropertyConstraint y) {
		
		List<Value> xList = x.getIn();
		List<Value> yList = y.getIn();
		
		if (xList!=null && yList!=null) {
			Set<Value> set = new HashSet<>();
			set.addAll(xList);
			set.addAll(yList);
			
			List<Value> list = new ArrayList<>(set);
			p.setIn(list);
		}
		
	}

	private Map<String, PropertyConstraint> mapProperties(Shape b) {
		Map<String, PropertyConstraint> map = new HashMap<>();
		for (PropertyConstraint c : b.getProperty()) {
			String key = c.getPredicate().stringValue();
			map.put(key, c);
		}
		
		return map;
	}
}
