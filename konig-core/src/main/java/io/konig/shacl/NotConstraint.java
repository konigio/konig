package io.konig.shacl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

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


import io.konig.core.Vertex;

public class NotConstraint implements Constraint {

	private Shape shape;
	private Shape declaringShape;
	
	public NotConstraint() {
		
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

	@Override
	public boolean accept(Vertex v) {
		return !GraphFilter.INSTANCE.matches(v, shape);
	}

	@Override
	public boolean hasPropertyConstraint(URI predicate) {
		
		return shape.hasPropertyConstraint(predicate);
	}

	@Override
	public Shape findShapeByTargetClass(URI targetClass) {
		
		return targetClass.equals(shape.getTargetClass()) ? shape : null;
	}

	@Override
	public List<Shape> getShapes() {
		List<Shape> result = new ArrayList<>();
		result.add(shape);
		return result;
	}

	@Override
	public Shape getDeclaringShape() {
		return declaringShape;
	}
	
	public void setDeclaringShape(Shape s) {
		declaringShape = s;
	}
	

}
