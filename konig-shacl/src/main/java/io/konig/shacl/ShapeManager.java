package io.konig.shacl;

import java.util.List;

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


import org.openrdf.model.URI;

import io.konig.core.UnnamedResourceException;

public interface ShapeManager {
	
	Shape getShapeById(URI shapeId);
	
	/**
	 * Get the list of all shapes known to this ShapeManager.
	 * @return The list of all shapes known to this ShapeManager.
	 */
	List<Shape> listShapes();
	
	/**
	 * Get the list of shapes that have a given scope class.
	 * @param targetClass The Class of interest
	 * @return The list of shapes that have a given scope class.
	 */
	List<Shape> getShapesByTargetClass(URI targetClass);
	
	/**
	 * Add a Shape to this manager.
	 * @param shape The shape being added.
	 * @throws UnnamedResourceException If the shape is identified by a BNode.
	 */
	void addShape(Shape shape) throws UnnamedResourceException;

}
