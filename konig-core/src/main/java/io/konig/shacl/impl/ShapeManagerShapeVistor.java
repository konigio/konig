package io.konig.shacl.impl;

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


import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeVisitor;

public class ShapeManagerShapeVistor implements ShapeVisitor {
	private ShapeManager shapeManager;
	

	public ShapeManagerShapeVistor(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}


	@Override
	public void visit(Shape shape) {
		shapeManager.addShape(shape);
	}

}