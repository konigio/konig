package io.konig.shacl;

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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class AndConstraint implements Constraint, ShapeConsumer {

	private Shape declaringShape;
	private List<Shape> shapes = new ArrayList<>();

	public List<Shape> getShapes() {
		return shapes;
	}
	
	public AndConstraint add(Shape shape) {
		if (!shapes.contains(shape)) {
			shapes.add(shape);
		}
		return this;
	}

	@Override
	public boolean accept(Vertex v) {
		
		GraphFilter filter = GraphFilter.INSTANCE;
		for (Shape s : shapes) {
			if (!filter.matches(v, s)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean hasPropertyConstraint(URI predicate) {
		
		for (Shape s : shapes) {
			if (s.hasPropertyConstraint(predicate)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Shape findShapeByTargetClass(URI targetClass) {
		for (Shape s : shapes) {
			if (targetClass.equals(s.getTargetClass())) {
				return s;
			}
		}
		return null;
	}

	@Override
	public Shape getDeclaringShape() {
		return declaringShape;
	}
	
	public void setDeclaringShape(Shape s) {
		declaringShape = s;
	}
}
