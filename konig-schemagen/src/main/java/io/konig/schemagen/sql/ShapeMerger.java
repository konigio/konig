package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A utility that clones a given shape and flattens the SHACL logical constraints by merging 
 * properties from those constraints into the direct properties.
 * @author Greg McFall
 *
 */
public class ShapeMerger {
	
	/**
	 * Merge the shapes from SHACL logical constraints (sh:or, sh:and, sh:not) into a single Shape suitable for
	 * generating a physical SQL table.
	 * @param shape
	 * @return
	 */
	public Shape merge(Shape shape) throws ShapeMergeException {
		Shape result = shape;
		if (shape.getAnd() != null) {
			throw new UnsupportedOperationException("Merging of Shape with sh:and constraint not supported");
		} else if (shape.getOr() != null) {
			result = shape.deepClone();
			mergeOr(shape, result);
		} else {
			result = shape;
		}
		
		return result;
	}


	private void mergeOr(Shape source, Shape target) throws ShapeMergeException {
		if (source.getOr()!=null) {
			for (Shape s : source.getOr().getShapes()) {
				mergeProperties(s, target);
			}
		}
	}

	private void mergeProperties(Shape source, Shape target) throws ShapeMergeException {
		
		for (PropertyConstraint sourceProperty : source.getProperty()) {
			URI predicate = sourceProperty.getPredicate();
			if (predicate == null) {
				throw new ShapeMergeException("Path type not supported: " + sourceProperty.getPath().getClass().getSimpleName());
			}
			
			PropertyConstraint targetProperty = target.getPropertyConstraint(predicate);
			if (targetProperty == null) {
				sourceProperty.setMinCount(0);
				target.add(sourceProperty);
			} else {
				merge(sourceProperty, targetProperty);
			}
		}
		
	}

	private void merge(PropertyConstraint sourceProperty, PropertyConstraint targetProperty) throws ShapeMergeException {
		
		assertCompatible(sourceProperty, targetProperty);
		
		Integer minCount = min(sourceProperty.getMinCount(), targetProperty.getMinCount());
		Integer maxCount = max(sourceProperty.getMaxCount(), targetProperty.getMaxCount());
		
		if (
			!safeEquals(minCount, targetProperty.getMinCount()) ||
			!safeEquals(maxCount, targetProperty.getMaxCount())
		) {
			targetProperty.setMaxCount(maxCount);
			targetProperty.setMinCount(minCount);
		}
		
		Shape sourceShape = sourceProperty.getShape();
		if (sourceShape != null) {
			if (targetProperty.getShape() == null) {
				merge(sourceShape, targetProperty.getShape());
			} else {
				targetProperty.setShape(sourceShape);
			}
		}
	}


	private void merge(Shape a, Shape b) throws ShapeMergeException {
		mergeOr(a, b);
		mergeProperties(a, b);
	}


	private Integer max(Integer a, Integer b) {
		if (a==null || b==null) {
			return null;
		}
		return Math.max(a, b);
	}

	private Integer min(Integer a, Integer b) {
		if (a==null || b==null) {
			return null;
		}
		return Math.min(a, b);
	}
	
	

	private void assertCompatible(PropertyConstraint sourceProperty, PropertyConstraint targetProperty) throws ShapeMergeException {
		
		
		if (!safeEquals(sourceProperty.getDatatype(), targetProperty.getDatatype())) {
			throw new ShapeMergeException("Incompatible datatype");
		}
		
		if (!safeEquals(valueClass(sourceProperty), valueClass(targetProperty))) {
			throw new ShapeMergeException("Incompatible Value Class");
		}
		
	}

	private boolean safeEquals(Object a, Object b) {
		return (a==null && b==null) || (a!=null && a.equals(b)) || (b!=null && b.equals(a));
	}

	private Resource valueClass(PropertyConstraint p) {
	
		if (p.getValueClass() != null) {
			return p.getValueClass();
		}
		Shape shape = p.getShape();
		
		return shape==null ? null : shape.getTargetClass();
	}

}
