package io.konig.schemagen;

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

/**
 * An interface that can transform shapes, typically for the purpose of constraining a physical
 * schema.
 * 
 * @author Greg McFall
 *
 */
public interface ShapeTransformer {
	
	/**
	 * Transform a PropertyConstraint for a given Shape
	 * @param shape  The shape whose PropertyConstraint is to be transformed
	 * @param constraint The constraint to be transformed
	 * @return The new version of the PropertyConstraint, or null if the property is to be omitted.
	 */
	PropertyConstraint transform(Shape shape, PropertyConstraint constraint);

}
