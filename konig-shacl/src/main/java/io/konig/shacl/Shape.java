package io.konig.shacl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.shacl.impl.EmptyList;

public class Shape {
	private static final List<PropertyConstraint> EMPTY_PROPERTY_LIST = new EmptyList<PropertyConstraint>();
	
	private Resource id;
	private URI scopeClass;
	private List<PropertyConstraint> property;
	
	public Shape(Resource id) {
		this.id = id;
	}
	
	public List<PropertyConstraint> getProperty() {
		return property==null ? EMPTY_PROPERTY_LIST : property;
	}
	
	/**
	 * Get the PropertyConstraint with the specified predicate.
	 * @param predicate The predicate of the requested PropertyConstraint
	 * @return The PropertyConstraint with the specified predicate, or null if this Shape does not contain such a property constraint.
	 */
	public PropertyConstraint property(URI predicate) {
		if (property != null) {
			for (PropertyConstraint p : property) {
				if (p.getPredicate().equals(predicate)) {
					return p;
				}
			}
		}
		return null;
	}
	
	public PropertyConstraint add(PropertyConstraint c) {
		if (property == null) {
			property  = new ArrayList<PropertyConstraint>();
		}
		for (PropertyConstraint p : property) {
			if (p.getPredicate().equals(c)) {
				return p;
			}
		}
		property.add(c);
		return c;
	}

	public URI getScopeClass() {
		return scopeClass;
	}

	public void setScopeClass(URI scopeClass) {
		this.scopeClass = scopeClass;
	}

	public Resource getId() {
		return id;
	}
	
	
	
	
	
	

}
