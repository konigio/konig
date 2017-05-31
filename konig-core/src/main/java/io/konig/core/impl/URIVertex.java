package io.konig.core.impl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class URIVertex implements URI, ResourceVertex {
	private static final long serialVersionUID = 1L;
	private Vertex vertex;
	private String stringValue;
	private String namespace;
	private String localName;
	
	public URIVertex(String iriValue, Vertex vertex) {
		if (iriValue.indexOf(':')<0) {
			throw new IllegalArgumentException("Invalid URI: " + iriValue);
		}
		stringValue = iriValue;
		this.vertex = vertex;
	}
	
	public Vertex getVertex() {
		return vertex;
	}

	public VertexImpl getVertexImpl() {
		return (VertexImpl) vertex;
	}

	@Override
	public String stringValue() {
		return stringValue;
	}

	@Override
	public String getNamespace() {
		if (namespace == null) {
			parse();
		}
		return namespace;
	}

	private void parse() {
		int slash = stringValue.lastIndexOf('/');
		int colon = stringValue.lastIndexOf(':');
		int hash = stringValue.lastIndexOf('#');
		
		int mark = Math.max(slash, colon);
		mark = Math.max(mark, hash);
		mark++;
		namespace = stringValue.substring(0, mark);
		localName = stringValue.substring(mark);
	}

	@Override
	public String getLocalName() {
		if (localName == null) {
			parse();
		}
		return localName;
	}

	// Implements URI.equals(Object)
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof URI) {
			return toString().equals(o.toString());
		}

		return false;
	}

	// Implements URI.hashCode()
	@Override
	public int hashCode() {
		return stringValue.hashCode();
	}
	
	@Override
	public String toString() {
		return stringValue;
	}
}
