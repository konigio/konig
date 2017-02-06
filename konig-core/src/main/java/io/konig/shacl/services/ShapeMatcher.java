package io.konig.shacl.services;

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


import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeValidator;

/**
 * A utility that finds the Shape that best matches a collection of instances of a given Class.
 * @author Greg McFall
 *
 */
public class ShapeMatcher {

	private ShapeManager shapeManager;
	private ShapeValidator validator;
	
	public ShapeMatcher(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
		validator = new ShapeValidator();
		validator.setClosed(true);
	}

	public Shape bestMatch(List<Vertex> individuals, URI owlClass) {
		
		List<Shape> candidates = shapeManager.getShapesByTargetClass(owlClass);
		Shape bestShape = null;
		int minPropertyCount = Integer.MAX_VALUE;
		
		for (Shape shape : candidates) {
			for (Vertex v : individuals) {
				if (validator.conforms(v, shape)) {
					int count = propertyCount(shape);
					if (bestShape == null || count<minPropertyCount) {
						bestShape = shape;
						minPropertyCount = count;
					} else if (bestShape!=null && count==minPropertyCount) {
						Resource shapeId = shape.getId();
						if (shapeId != null) {
							Resource bestShapeId = bestShape.getId();
							if (bestShapeId==null || shapeId.stringValue().compareTo(bestShapeId.stringValue())<0) {
								bestShape = shape;
								minPropertyCount = count;
							}
						}
					}
				}
			}
		}
		
		
		return bestShape;
	}

	private int propertyCount(Shape shape) {
		int count = 0;
		for (PropertyConstraint p : shape.getProperty()) {
			if (p.getPredicate() != null) {
				count++;
				Shape childShape = p.getShape();
				if (childShape != null) {
					count += propertyCount(childShape);
				}
			}
		}
		
		return count;
	}
}
