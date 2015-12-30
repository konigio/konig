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
