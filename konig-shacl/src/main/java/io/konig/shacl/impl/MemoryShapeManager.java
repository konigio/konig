package io.konig.shacl.impl;

import java.io.InputStream;
import java.util.ArrayList;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.ContextManager;
import io.konig.core.UnnamedResourceException;
import io.konig.core.io.JsonldParser;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeRdfHandler;

public class MemoryShapeManager implements ShapeManager {
	private Map<String, Shape> shapeMap = new HashMap<String, Shape>();

	public Shape getShapeById(URI shapeId) {
		return shapeMap.get(shapeId.stringValue());
	}

	public void addShape(Shape shape) throws UnnamedResourceException {
		
		if (shape.getId() instanceof BNode) {
			throw new UnnamedResourceException("Cannot add unnamed Shape to this manager");
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
