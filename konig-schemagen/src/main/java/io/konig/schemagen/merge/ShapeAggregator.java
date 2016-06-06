package io.konig.schemagen.merge;

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
		
		Resource aId = x.getValueShapeId();
		Resource bId = y.getValueShapeId();
		
		if (aId !=null && bId==null) {
			setValueShape(p, aId, x.getValueShape());
		} else if (aId==null && bId!=null) {
			setValueShape(p, bId, y.getValueShape());
		} else if (aId!=null && bId!=null) {
			Shape c = valueShape(x);
			Shape d = valueShape(y);
			StringBuilder builder = new StringBuilder(parent.getId().stringValue());
			builder.append('.');
			builder.append(p.getPredicate().getLocalName());
			
			URI uri = new URIImpl(builder.toString());
			
			p.setValueShapeId(uri);
			Shape valueShape = merge(uri, c, d);
			p.setValueShape(valueShape);
			if (shapeManager != null) {
				shapeManager.addShape(valueShape);
			}
			
		}
		
	}

	private Shape valueShape(PropertyConstraint x) {
		Shape s = x.getValueShape();
		if (s == null) {
			if (shapeManager == null) {
				throw new SchemaGeneratorException("shapeManager is not defined");
			}
			
			Resource id = x.getValueShapeId();
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
		if (id instanceof URI) {
			URI uri = (URI) id;
			p.setValueShapeId(uri);
			
			if (shape == null && shapeManager!=null) {
				shape = shapeManager.getShapeById(uri);
			}
			
		} else {
			throw new SchemaGeneratorException("Blank node not supported for value shape");
		}

		p.setValueShape(shape);
		
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
		
		List<Value> xList = x.getAllowedValues();
		List<Value> yList = y.getAllowedValues();
		
		if (xList!=null && yList!=null) {
			Set<Value> set = new HashSet<>();
			set.addAll(xList);
			set.addAll(yList);
			
			List<Value> list = new ArrayList<>(set);
			p.setAllowedValue(list);
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
