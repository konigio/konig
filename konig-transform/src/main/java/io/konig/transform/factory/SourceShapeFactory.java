package io.konig.transform.factory;

/*
 * #%L
 * Konig Transform
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

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SourceShapeFactory extends ShapeNodeFactory<SourceShape, SourceProperty> {
	
	public static final SourceShapeFactory INSTANCE = new SourceShapeFactory();

	@Override
	protected SourceShape shape(Shape shape) {
		return new SourceShape(shape);
	}

	@Override
	protected SourceProperty property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch) {
		return new SourceProperty(p, pathIndex);
	}

	@Override
	protected SourceProperty property(PropertyConstraint p, int pathIndex, URI predicate, Value value) {
		return new SourceProperty(p, pathIndex, predicate, value);
	}
	public SourceShape createShapeNode(Shape shape) {
		SourceShape result = super.createShapeNode(shape);
		List<PropertyConstraint> list = shape.getDerivedProperty();
		if (list != null) {
			this.addProperties(result, list, true);
		}
		return result;
	}

}
