package io.konig.shacl;

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
	private Shape shape;

	public ShapeBuilder(Shape shape) {
		shapeManager = new MemoryShapeManager();
		this.shape = shape;
		shapeManager.addShape(shape);
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
		return this.shape(new URIImpl(shapeIRI));
	}
	
	public ShapeBuilder scopeClass(URI type) {
		shape.setScopeClass(type);
		return this;
	}
	
	public PropertyBuilder property(URI predicate) {
		BNode id = valueFactory.createBNode();
		PropertyConstraint p = new PropertyConstraint(id, predicate);
		shape.add(p);
		return new PropertyBuilder(this, p);
	}
	
	public ShapeBuilder shape(URI shapeId) {
		shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			shape = new Shape(shapeId);
			shapeManager.addShape(shape);
		}
		return this;
	}
	
	
	public Shape shape() {
		return shape;
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
	}

}
