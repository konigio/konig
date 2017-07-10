package io.konig.schemagen.domain;

/*
 * #%L
 * Konig Schema Generator
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


import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShapeProperty {
	private Shape shape;
	private PropertyConstraint constraint;
	public ShapeProperty(Shape shape, PropertyConstraint constraint) {
		this.shape = shape;
		this.constraint = constraint;
	}
	public Shape getShape() {
		return shape;
	}
	public PropertyConstraint getConstraint() {
		return constraint;
	}
	
	public boolean equals(Object other) {
		if (other instanceof ShapeProperty) {
			ShapeProperty s = (ShapeProperty) other;
			return s.shape==shape && s.constraint==constraint;
		}
		return false;
	}

}
