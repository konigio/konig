package io.konig.shacl.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;

public class GraphNodeShape implements Traversable {
	private GraphPropertyShape accessor;
	private GraphOwlClass owlClass;
	private Shape shape;
	

	private Map<URI,GraphPropertyShape> properties = new HashMap<>();
	
	public GraphNodeShape(GraphPropertyShape accessor, Shape shape) {
		this.accessor = accessor;
		this.shape = shape;
	}
	
	public Collection<GraphPropertyShape> getProperties() {
		return properties.values();
	}


	public void addProperty(GraphPropertyShape p) {
		properties.put(p.getPredicate(), p);
	}
	
	public GraphPropertyShape getProperty(URI predicate) {
		return properties.get(predicate);
	}

	public Shape getShape() {
		return shape;
	}

	

	public GraphOwlClass getOwlClass() {
		return owlClass;
	}


	public void setOwlClass(GraphOwlClass owlClass) {
		this.owlClass = owlClass;
	}


	public boolean hasAncestor(Resource shapeId) {
		if (shape.getId().equals(shapeId)) {
			return true;
		}
		if (accessor != null) {
			return accessor.getDeclaringShape().hasAncestor(shapeId);
		}
		return false;
	}

	public GraphPropertyShape getAccessor() {
		return accessor;
	}


	@Override
	public String getPath() {
		if (accessor == null) {
			return "{" + RdfUtil.localName(shape.getId()) + "}";
		}
		return accessor.getPath();
	}
	
	public String toString() {
		return getPath();
	}

	
	


}
