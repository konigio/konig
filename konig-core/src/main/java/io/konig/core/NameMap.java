package io.konig.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class NameMap extends HashMap<String,URI> {
	private static final long serialVersionUID = 1L;
	public static URI AMBIGUOUS = new URIImpl("urn:ambiguous");
	
	private Map<String, Set<URI>> ambiguous = new HashMap<>();
	
	public NameMap() {
		
	}
	
	public NameMap(Graph graph) {
		addAll(graph);
	}
	

	
	public void addStaticFields(Class<?> type) {
		Field[] declaredFields = type.getDeclaredFields();
		for (Field field : declaredFields) {
		    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
		        if (URI.class.isAssignableFrom(field.getType())) {
		        	try {
						URI value = (URI) field.get(null);
						put(value.getLocalName(), value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new KonigException(e);
					}
		        	
		        }
		    }
		}
	}
	
	public void addAll(Graph graph) {
		for (Edge edge : graph) {
			Resource subject = edge.getSubject();
			URI predicate = edge.getPredicate();
			Value object = edge.getObject();
			
			add(subject);
			add(predicate);
			add(object);
		}
	}
	
	public void addShapes(Collection<Shape> shapeList) {
		for (Shape s : shapeList) {
			addProperties(s.getProperty());
		}
	}

	private void addProperties(List<PropertyConstraint> property) {
		if (property != null) {
			for (PropertyConstraint c : property) {
				add(c.getPredicate());
			}
		}
		
	}

	public void add(Value value) {
		if (value instanceof URI) {
			URI uri = (URI) value;
			String localName = uri.getLocalName();
			put(localName, uri);
		}
	}
	
	public URI put(String name, URI uri) {
		URI result = super.put(name, uri);
		if (result != null && !result.equals(uri)) {
			
			if (ambiguous==null) {
				ambiguous = new HashMap<>();
			}
			Set<URI> set = null;
			if (result != AMBIGUOUS) {
				super.put(name, AMBIGUOUS);
				ambiguous.put(name, set=new HashSet<>());
			} else {
				result = AMBIGUOUS;
				set = ambiguous.get(name);
				set.add(uri);
			}
			
		}
		
		return result;
	}
	
	/**
	 * Get all URIs with a given local name.
	 * @param name The local name.
	 * @return The set of all URIs with the given local name. If no URIs have the local name, an
	 * empty set is returned.
	 */
	@SuppressWarnings("unchecked")
	public Set<URI> getAll(String name) {
		Set<URI> result = ambiguous.get(name);
		
		if (result == null) {
			URI unique = get(name);
			if (unique != null) {
				result = new HashSet<>();
				result.add(unique);
			}
		}
		
		return (Set<URI>) (result == null ? Collections.emptySet() : result);
	}
	
	/**
	 * A helper method that appends all the URIs from the given set to a StringBuilder, using
	 * CURIE values where possible.
	 * @param builder The StringBuilder to which URI values will be appended.
	 * @param nsManager The NamespaceManager used to lookup namespace prefixes for CURIE values
	 * @param set The URI values to be appended to the StringBuilder.
	 */
	public static void appendAll(StringBuilder builder, NamespaceManager nsManager, Set<URI> set) {
		List<String> list = new ArrayList<>();
		for (URI uri : set) {
			StringBuilder name = new StringBuilder();
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns == null) {
				name.append('<');
				name.append(uri.stringValue());
				name.append('>');
			} else {
				name.append(ns.getPrefix());
				name.append(':');
				name.append(uri.getLocalName());
			}
			list.add(name.toString());
		}
		Collections.sort(list);
		String comma = "";
		for (String name : list) {
			builder.append(comma);
			builder.append(name);
			comma = ", ";
		}
	}
	
	/**
	 * Get the unique URI with a given local name.
	 * This method returns null if there is no unique URI with the given local name.
	 * In that case, use {@link #getAll(String) getAll} to find all URIs with the the supplied name.
	 * @param name The local name
	 * @return The unique URI if one exists, and null otherwise.
	 */
	public URI get(String name) {
		URI result = super.get(name);
		if (result == AMBIGUOUS) {
			result = null;
		}
		return result;
	}

}
