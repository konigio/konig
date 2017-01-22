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
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import io.konig.activity.Activity;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeBuilder {
	
	private PropertyBuilder propertyBuilder;
	private ShapeManager shapeManager;
	private ValueFactory valueFactory = new ValueFactoryImpl();
	
	private List<Object> stack = new ArrayList<>();


	public ShapeBuilder(PropertyBuilder propertyBuilder, ShapeManager shapeManager, 
			ValueFactory valueFactory, Shape shape) {
		this.propertyBuilder = propertyBuilder;
		this.shapeManager = shapeManager;
		this.valueFactory = valueFactory;
		stack.add(shape);
	}
	
	public ShapeBuilder(ShapeManager shapeManager, ValueFactory valueFactory, Shape shape) {
		this.shapeManager = shapeManager;
		this.valueFactory = valueFactory;
		stack.add(shape);
	}
	
	public ShapeBuilder nodeKind(NodeKind kind) {
		peekShape().setNodeKind(kind);
		return this;
	}
	
	public PropertyBuilder endValueShape() {
		return propertyBuilder;
	}
	

	
	public Shape getShape(URI uri) {
		return shapeManager.getShapeById(uri);
	}
	
	public Shape getShape(String uri) {
		return shapeManager.getShapeById(new URIImpl(uri));
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
	
	public ShapeBuilder or(Resource...shapeId) {
		OrConstraint constraint = peekShape().getOr();
		if (constraint == null) {
			constraint = new OrConstraint();
			peekShape().setOr(constraint);
		}
		for (Resource id : shapeId) {
			Shape shape = shapeManager.getShapeById(id);
			if (shape == null) {
				shape = new Shape(id);
				shapeManager.addShape(shape);
			}
			constraint.add(shape);
		}
		
		return this;
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
	
	public ShapeBuilder documentation(String text) {
		return this;
	}
	
	public ShapeBuilder shape(String shapeIRI) {
		return this.beginShape(new URIImpl(shapeIRI));
	}
	
	public ShapeBuilder targetClass(URI type) {
		peekShape().setTargetClass(type);
		return this;
	}
	
	public PropertyBuilder beginProperty(URI predicate) {
		return property(predicate);
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
	
	public ShapeBuilder wasGeneratedBy(Activity activity) {
	
		Shape shape = peekShape();
		shape.setWasGeneratedBy(activity);
		
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
	
	public ShapeBuilder beginShape(Resource resource) {
		Shape shape = new Shape(resource);
		
		if (resource instanceof URI) {
			shapeManager.addShape(shape);
		}
		
		ShapeConsumer consumer = peekConsumer();
		if (consumer != null) {
			consumer.add(shape);
		}
		stack.add(shape);
		
		return this;
	}
	
	public ShapeBuilder beginShape(String iri) {
		URI uri = valueFactory.createURI(iri);
		return beginShape(uri);
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
		
		public ShapeBuilder beginValueShape(String iri) {
			URI uri = parent.valueFactory.createURI(iri);
			return beginValueShape(uri);
		}
		
		public ShapeBuilder beginValueShape() {
			return beginValueShape((URI)null);
		}

		
		public ShapeBuilder endProperty() {
			return parent;
		}
		
		public PropertyBuilder endValueShape() {
			return parent.endValueShape();
		}
		
		
		public ShapeBuilder beginValueShape(URI shapeId) {
			Shape shape = null;
			
			if (shapeId != null) {
				shape = parent.shapeManager.getShapeById(shapeId);
			}
			
			if (shape == null) {
				shape = new Shape(shapeId);
				if (shapeId != null) {
					parent.shapeManager.addShape(shape);
				}
			}
			
			property.setShape(shape);
			
			return new ShapeBuilder(this, parent.shapeManager, parent.valueFactory, shape);
		}
		
		public PropertyBuilder stereotype(URI stereotype) {
			property.setStereotype(stereotype);
			return this;
		}
		
		public PropertyBuilder isTimeParam(boolean truth) {
			property.setTimeParam(truth);
			return this;
		}
		
		public PropertyBuilder documentation(String text) {
			property.setDocumentation(text);
			return this;
		}
		
		public PropertyBuilder nodeKind(NodeKind kind) {
			property.setNodeKind(kind);
			return this;
		}
		
		public PropertyBuilder allowedValue(Value value) {
			property.addIn(value);
			return this;
		}
		
		public PropertyBuilder allowedIRI(String iri) {
			property.addIn(new URIImpl(iri));
			return this;
		}
		
		public PropertyBuilder minInclusive(double value) {
			property.setMinInclusive(value);
			return this;
		}
		
		public PropertyBuilder maxInclusive(double value) {
			property.setMaxInclusive(value);
			return this;
		}
		
		public PropertyBuilder datatype(URI type) {
			property.setDatatype(type);
			return this;
		}
		
		public PropertyBuilder dimension(URI dimension) {
			property.setDimensionTerm(dimension);
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
		
		public PropertyBuilder equivalentPath(String value) {
			property.setEquivalentPath(value);
			return this;
		}
		
		public PropertyBuilder valueShape(URI shapeId) {
			
			Shape shape = parent.shapeManager.getShapeById(shapeId);
			if (shape == null) {
				shape = new Shape(shapeId);
				parent.shapeManager.addShape(shape);
			}
			property.setShape(shape);
			return this;
		}
		
		public PropertyBuilder valueClass(URI type) {
			property.setValueClass(type);
			return this;
		}
		
		public PropertyBuilder directType(URI type) {
			property.setDirectValueType(type);
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

		public PropertyBuilder in(Object...objects) {
			List<Value> list = new ArrayList<>();
			for (Object v : objects) {
				if (v instanceof String) {
					String text = (String) v;
					list.add(new LiteralImpl(text));
				} else if (v instanceof URI) {
					list.add((URI)v);
				}
			}
			property.setIn(list);
			return this;
		}

	}

}
