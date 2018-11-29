package io.konig.core.showl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;

public class ShowlNodeShape implements Traversable {
	private ShowlPropertyShape accessor;
	private ShowlClass owlClass;
	private Shape shape;
	

	private Map<URI,ShowlPropertyShape> properties = new HashMap<>();
	private Map<URI, ShowlDerivedPropertyShape> derivedProperties = new HashMap<>();
	
	public ShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {
		this.accessor = accessor;
		this.shape = shape;
		this.owlClass = owlClass;
		if (accessor != null) {
			accessor.setValueShape(this);
		}
	}
	
	public Collection<ShowlPropertyShape> getProperties() {
		return properties.values();
	}
	
	public Set<ShowlPropertyShape> allProperties() {
		Set<ShowlPropertyShape> set = new HashSet<>();
		set.addAll(getProperties());
		set.addAll(getDerivedProperties());
		return set;
	}


	public void addProperty(ShowlPropertyShape p) {
		properties.put(p.getPredicate(), p);
	}
	
	public ShowlPropertyShape getProperty(URI predicate) {
		return properties.get(predicate);
	}
	
	public ShowlDerivedPropertyShape getDerivedProperty(URI predicate) {
		return derivedProperties.get(predicate);
	}

	public Shape getShape() {
		return shape;
	}

	

	public ShowlClass getOwlClass() {
		return owlClass;
	}


	public void setOwlClass(ShowlClass owlClass) {
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

	public ShowlPropertyShape getAccessor() {
		return accessor;
	}
	
	public void addDerivedProperty(ShowlDerivedPropertyShape p) {
		derivedProperties.put(p.getPredicate(), p);
	}
	
	public ShowlPropertyShape findProperty(URI predicate) {
		ShowlPropertyShape p = getProperty(predicate);
		if (p == null) {
			p = getDerivedProperty(predicate);
		}
		return p;
	}

	public Collection<ShowlDerivedPropertyShape> getDerivedProperties() {
		return derivedProperties.values();
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
