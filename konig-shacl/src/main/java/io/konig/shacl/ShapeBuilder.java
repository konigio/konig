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

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeBuilder {
	
	private ShapeManager shapeManager;
	private ValueFactory valueFactory = new ValueFactoryImpl();
	
	private List<Object> stack = new ArrayList<>();

	
	public ShapeBuilder(ShapeManager shapeManager, ValueFactory valueFactory, Shape shape) {
		this.shapeManager = shapeManager;
		this.valueFactory = valueFactory;
		stack.add(shape);
	}
	
	private ShapeConsumer peekConsumer() {
		Object result = peek();
		return (result instanceof ShapeConsumer) ? (ShapeConsumer) result : null;
	}
	
	private Object peek() {
		return stack.isEmpty() ? null : stack.get(stack.size()-1);
	}
	
	private Shape peekShape() {
		Object result = peek();
		return (result instanceof Shape) ? (Shape) result : null;
	}


	public ShapeBuilder(Shape shape) {
		shapeManager = new MemoryShapeManager();
		stack.add(shape);
		shapeManager.addShape(shape);
	}
	
	
	public ShapeBuilder() {
		shapeManager = new MemoryShapeManager();
	}
	
	public ShapeBuilder(String shapeId) {
		this(new URIImpl(shapeId));
	}
	
	public ShapeBuilder(Resource shapeId) {
		this(new Shape(shapeId));
	}
	
	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	public ShapeBuilder shape(String shapeIRI) {
		return this.beginShape(new URIImpl(shapeIRI));
	}
	
	public ShapeBuilder scopeClass(URI type) {
		peekShape().setScopeClass(type);
		return this;
	}
	
	public PropertyBuilder property(URI predicate) {
		BNode id = valueFactory.createBNode();
		PropertyConstraint p = new PropertyConstraint(id, predicate);
		peekShape().add(p);
		return new PropertyBuilder(this, p);
	}
	
	public ShapeBuilder beginShape(URI shapeId) {
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			shape = new Shape(shapeId);
			shapeManager.addShape(shape);
		}
		stack.add(shape);
		return this;
	}
	
	public ShapeBuilder beginOr() {
		OrConstraint constraint = new OrConstraint();
		peekShape().setConstraint(constraint);
		stack.add(constraint);
		
		return this;
	}
	
	public ShapeBuilder endOr() {
		return pop();
	}
	
	public ShapeBuilder beginAnd() {
		AndConstraint constraint = new AndConstraint();
		peekShape().setConstraint(constraint);
		stack.add(constraint);
		
		return this;
	}
	
	public ShapeBuilder endAnd() {
		return pop();
	}
	
	
	public ShapeBuilder beginShape() {
		BNode shapeId = valueFactory.createBNode();
		Shape shape = new Shape(shapeId);
		
		ShapeConsumer consumer = peekConsumer();
		if (consumer != null) {
			consumer.add(shape);
		}
		
		stack.add(shape);
		return this;
	}
	
	public ShapeBuilder endShape() {
		return pop();
	}
	

	
	private ShapeBuilder pop() {
		if (!stack.isEmpty()) {
			stack.remove(stack.size()-1);
		}
		return this;
	}
	
	
	public Shape shape() {
		return peekShape();
	}
	

	
	
	
	static public class PropertyBuilder {
		private ShapeBuilder parent;
		private PropertyConstraint property;
		PropertyBuilder(ShapeBuilder parent, PropertyConstraint property) {
			this.parent = parent;
			this.property = property;
		}
		
		public PropertyBuilder datatype(URI type) {
			property.setDatatype(type);
			return this;
		}
		
		public PropertyBuilder maxCount(int value) {
			property.setMaxCount(value);
			return this;
		}
		
		public PropertyBuilder minCount(int value) {
			property.setMinCount(value);
			return this;
		}
		
		public PropertyBuilder valueShape(URI shapeId) {
			property.setValueShapeId(shapeId);
			return this;
		}
		
		public PropertyBuilder type(URI type) {
			property.setType(type);
			return this;
		}
		
		public PropertyBuilder directType(URI type) {
			property.setDirectType(type);
			return this;
		}
		
		public PropertyBuilder property(URI predicate) {
			return parent.property(predicate);
		}
		
		public ShapeBuilder shape(String shapeIRI) {
			return parent.shape(shapeIRI);
		}
		
		public Shape shape() {
			return parent.shape();
		}
		
		public ShapeBuilder endShape() {
			return parent.endShape();
		}
	}

}
