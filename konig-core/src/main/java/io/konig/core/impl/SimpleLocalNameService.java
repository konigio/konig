package io.konig.core.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.LocalNameService;
import io.konig.core.Vertex;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OMCS;
import io.konig.core.vocab.PROV;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SimpleLocalNameService implements LocalNameService {
	
	private static final SimpleLocalNameService DEFAULT = new SimpleLocalNameService();
	
	static {
		DEFAULT.addStaticFields(Konig.class);
		DEFAULT.addStaticFields(SH.class);
		DEFAULT.addStaticFields(AS.class);
		DEFAULT.addStaticFields(Schema.class);
		DEFAULT.addStaticFields(PROV.class);
		DEFAULT.addStaticFields(RDF.class);
		DEFAULT.addStaticFields(RDFS.class);
		DEFAULT.addStaticFields(GCP.class);
		DEFAULT.addStaticFields(OMCS.class);
	}
	
	public static SimpleLocalNameService getDefaultInstance() {
		return DEFAULT;
	}
	
	private static final Set<URI> EMPTYSET = new HashSet<>();
	
	private Map<String, Set<URI>> map = new HashMap<>();

	public SimpleLocalNameService() {
	}
	
	public void addAll(Graph graph) {
		for (Vertex v : graph.vertices()) {
			Resource id = v.getId();
			if (id instanceof URI) {
				URI uri = (URI) id;
				add(uri);
			}
		}
	}
	
	public void addShapes(Collection<Shape> shapeList) {
		for (Shape s : shapeList) {
			addPropertyConstraints(s.getProperty());
		}
	}
	
	
	private void addPropertyConstraints(Collection<PropertyConstraint> propertyList) {
		for (PropertyConstraint p : propertyList) {
			safeAdd(p.getPredicate());
		}
		
	}

	private void safeAdd(URI uri) {
		if (uri != null) {
			add(uri);
		}
		
	}

	public void add(URI uri) {
		add(uri.getLocalName(), uri);
	}
	
	public void add(String localName, URI uri) {
		Set<URI> set = map.get(localName);
		if (set == null) {
			set = new HashSet<>();
			map.put(localName, set);
		}
		set.add(uri);
	}

	@Override
	public Set<URI> lookupLocalName(String localName) {
		Set<URI> result = map.get(localName);
		return result==null ? EMPTYSET : result;
	}
	
	/**
	 * Use reflection to scan a Java Class for static fields of type URI, and register
	 * those URI values.
	 * @param javaClass  The Java Class to be scanned.
	 */
	public void addStaticFields(Class<?> javaClass) {
		Field[] declaredFields = javaClass.getDeclaredFields();
		for (Field field : declaredFields) {
			if (
				Modifier.isStatic(field.getModifiers()) && 
				URI.class.isAssignableFrom(field.getType())
			) {
				try {
					URI value = (URI) field.get(null);
					add(value);
					
				} catch (Throwable e) {
					// Ignore
				}
			}
		}
	}

}
