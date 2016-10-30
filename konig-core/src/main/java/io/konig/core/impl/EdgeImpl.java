package io.konig.core.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

/*
 * #%L
 * konig-core
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;

import io.konig.core.Edge;
import io.konig.core.ValueSet;

public class EdgeImpl extends ContextStatementImpl implements Edge  {
	private static final long serialVersionUID = 1L;
	
	private HashMap<URI,Value> properties;
	public EdgeImpl(Resource subject, URI predicate, Value object) {
		super(subject, predicate, object, null);
	}
	
	public EdgeImpl(Resource subject, URI predicate, Value object, Resource context) {
		super(subject, predicate, object, context);
	}
	
	public EdgeImpl(Edge other) {
		super(other.getSubject(), other.getPredicate(), other.getObject(), other.getContext());
	}
	
	@Override
	public Value getAnnotation(URI predicate) {
		Value value = properties==null ? null : properties.get(predicate);
		if (value instanceof ValueSet) {
			ValueSet set = (ValueSet) value;
			if (set.isEmpty()) {
				value = null;
			} else if (set.size()==1) {
				value = set.iterator().next();
			}
		}
		return value;
	}
	@Override
	public Edge setAnnotation(URI predicate, Value value) {
		if (properties == null) {
			properties = new LinkedHashMap<>();
		}
		properties.put(predicate, value);
		return this;
	}

	@Override
	public Value removeAnnotation(URI predicate) {
		return properties==null ? null : properties.remove(predicate);
	}

	@Override
	public void copyAnnotations(Edge edge) {
		if (properties != null) {
			Set<Entry<URI,Value>> set = properties.entrySet();
			for (Entry<URI,Value> e : set) {
				edge.setAnnotation(e.getKey(), e.getValue());
			}
		}
		
	}

	@Override
	public ValueSet getAnnotationSet(URI predicate) {
		Value result = getAnnotation(predicate);
		if (!(result instanceof ValueSet)) {
			ValueSet set = new ValueSet();
			if (result != null) {
				set.add(result);
			}
			result = set;
		}
		return (ValueSet) result;
	}

	@Override
	public Edge addAnnotation(URI predicate, Value value) {
		ValueSet set = null;
		Value result = getAnnotation(predicate);
		if (result instanceof ValueSet) {
			set = (ValueSet) result;
		} else if (result == null) {
			setAnnotation(predicate, value);
			return this;
		} else {
			set = new ValueSet();
			set.add(result);
			setAnnotation(predicate, set);
			
		}

		set.add(value);
		return this;
	}

	@Override
	public boolean matches(Value a, Value b) {
		if (a instanceof ValueSet) {
			ValueSet set = (ValueSet) a;
			return set.contains(b);
		}
		
		if (b instanceof ValueSet) {
			ValueSet set = (ValueSet) b;
			return set.contains(a);
		}
		return a.equals(b);
	}
}
