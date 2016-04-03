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

public class OrConstraint implements Constraint, ShapeConsumer {

	private List<Shape> shapes = new ArrayList<>();

	public List<Shape> getShapes() {
		return shapes;
	}
	
	@Override
	public OrConstraint add(Shape shape) {
		shapes.add(shape);
		return this;
	}

	@Override
	public boolean accept(Vertex v) {
		GraphFilter filter = GraphFilter.INSTANCE;
		for (Shape s : shapes) {
			if (filter.matches(v, s)) {
				return true;
			}
		}
		return false;
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
}
