package io.konig.shacl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;

import io.konig.core.UidGenerator;
import io.konig.core.impl.UidGeneratorImpl;
import io.konig.shacl.impl.EmptyList;

public class Shape {
	private static final List<PropertyConstraint> EMPTY_PROPERTY_LIST = new EmptyList<PropertyConstraint>();
	
	private Resource id;
	private URI scopeClass;
	private List<PropertyConstraint> property;
	private Constraint constraint;
	
	public Shape() {
		String bnodeId = UidGenerator.INSTANCE.next();
		id = new BNodeImpl(bnodeId);
	}
	
	public Shape(Resource id) {
		this.id = id;
	}
	
	public List<PropertyConstraint> getProperty() {
		return property==null ? EMPTY_PROPERTY_LIST : property;
	}
	
	public Constraint getConstraint() {
		return constraint;
	}
	
	public Shape setConstraint(Constraint constraint) {
		this.constraint = constraint;
		return this;
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
	
	public Shape add(PropertyConstraint c) {
		if (property == null) {
			property  = new ArrayList<PropertyConstraint>();
		}
		for (PropertyConstraint p : property) {
			if (p.getPredicate().equals(c)) {
				return this;
			}
		}
		property.add(c);
		return this;
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
