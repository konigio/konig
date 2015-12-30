package io.konig.shacl.impl;

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
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;

import io.konig.core.UnnamedResourceException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

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

}
