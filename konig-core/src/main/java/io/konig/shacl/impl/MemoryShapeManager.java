package io.konig.shacl.impl;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class MemoryShapeManager implements ShapeManager {
	private Map<String, Shape> shapeMap = new LinkedHashMap<String, Shape>();

	public Shape getShapeById(Resource shapeId) {
		return shapeMap.get(shapeId.stringValue());
	}

	public void addShape(Shape shape)  {
		if (shape.getId() == null) {
			throw new KonigException("Shape has no id value");
		}
		
		shapeMap.put(shape.getId().stringValue(), shape);
	}

	public List<Shape> getShapesByPredicate(URI predicate) {
		List<Shape> list = new ArrayList<>();
		
		for (Shape s : shapeMap.values()) {
			if (s.hasPropertyConstraint(predicate)) {
				list.add(s);
			}
		}
		
		return list;
	}
	
	/**
	 * Scan the shapes in the manager and set the valueShapes so that the reference a Shape object.
	 */
	public void link() {
		List<Shape> list = new ArrayList<>( shapeMap.values() );
		for (Shape shape : list) {
			List<PropertyConstraint> constraints = shape.getProperty();
			for (PropertyConstraint p : constraints) {
				Resource resource = p.getShapeId();
				if (resource instanceof URI) {
					URI uri = (URI) resource;
					Shape valueShape = getShapeById(uri);
					if (valueShape == null) {
						throw new KonigException("Shape not found: " + uri);
					}
					p.setShape(valueShape);
				}
			}
		}
	}

	@Override
	public List<Shape> getShapesByTargetClass(URI targetClass) {
		List<Shape> list = new ArrayList<>();
		for (Shape s : shapeMap.values()) {
			if (targetClass.equals(s.getTargetClass())) {
				list.add(s);
			}
		}
		return list;
	}

	@Override
	public List<Shape> listShapes() {
		return new ArrayList<>(shapeMap.values());
	}
	

}
