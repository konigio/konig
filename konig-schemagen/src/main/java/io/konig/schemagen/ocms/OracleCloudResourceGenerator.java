package io.konig.schemagen.ocms;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import io.konig.core.KonigException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;
import io.konig.shacl.ShapeVisitor;

public class OracleCloudResourceGenerator {
	
	private List<ShapeVisitor> visitors = new ArrayList<>();
	
	public void add(ShapeVisitor visitor) {
		if (visitor != null) {
			visitors.add(visitor);
		}
	}
	
	public void dispatch(List<Shape> shapeList) throws KonigException {
		beginTraversal();
		for (Shape shape : shapeList) {
			for (ShapeVisitor visitor : visitors) {
				visitor.visit(shape);
			}
		}
		endTraversal();
	}

	private void endTraversal() {

		for (ShapeVisitor visitor : visitors) {
			if (visitor instanceof ShapeHandler) {
				ShapeHandler handler = (ShapeHandler) visitor;
				handler.endShapeTraversal();
			}
		}
		
	}

	private void beginTraversal() {
		for (ShapeVisitor visitor : visitors) {
			if (visitor instanceof ShapeHandler) {
				ShapeHandler handler = (ShapeHandler) visitor;
				handler.beginShapeTraversal();
			}
		}
		
	}
}
