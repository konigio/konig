package io.konig.core.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;

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
import org.openrdf.model.impl.StatementImpl;

import io.konig.core.Edge;

public class EdgeImpl extends StatementImpl implements Edge  {
	private static final long serialVersionUID = 1L;
	
	private HashMap<URI,Value> properties;
	public EdgeImpl(Resource subject, URI predicate, Value object) {
		super(subject, predicate, object);
	}
	
	public EdgeImpl(Edge other) {
		super(other.getSubject(), other.getPredicate(), other.getObject());
	}
	
	@Override
	public Value getProperty(URI predicate) {
		return properties==null ? null : properties.get(predicate);
	}
	@Override
	public void setProperty(URI predicate, Value value) {
		if (properties == null) {
			properties = new LinkedHashMap<>();
		}
		properties.put(predicate, value);
		
	}

	@Override
	public Value removeProperty(URI predicate) {
		return properties==null ? null : properties.remove(predicate);
	}
}
