package io.konig.core.pojo;

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


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.annotation.RdfProperty;
import io.konig.core.Graph;
import io.konig.core.LocalNameService;
import io.konig.core.NamespaceManager;

public class EmitContext {
	private static final URI NULL = new URIImpl("http://www.konig.io/NULL");
	private static final String NAME_NOT_FOUND = "Name not found";
	private static final String AMBIGUOUS_NAME = "The name is ambiguous";
	private static final Logger logger = LoggerFactory.getLogger(EmitContext.class);
	

	private NamespaceManager nsManager;
	private LocalNameService nameService;
	private Map<Method, URI> methodMap = new HashMap<>();
	private Map<Method, String> ignoredMethod = new HashMap<>();
	private Set<Class<?>> memory = new HashSet<>();
	private Set<Class<?>> skipClass = new HashSet<>();
	private Set<URI> iriReference = null;
	
	private Set<URI> ignoredProperty = null;
	
	private ValueFactory valueFactory = new ValueFactoryImpl();

	public EmitContext(Graph graph) {
		this(graph.getNamespaceManager(), graph);
	}
	
	public EmitContext(NamespaceManager nsManager, LocalNameService nameService) {
		this.nsManager = nsManager;
		this.nameService = nameService;
		skipClass.add(String.class);
		skipClass.add(int.class);
		skipClass.add(Integer.class);
		skipClass.add(double.class);
		skipClass.add(Double.class);
		skipClass.add(long.class);
		skipClass.add(Long.class);
		
	}
	
	public void addIriReference(URI... predicate) {
		if (iriReference == null) {
			iriReference = new HashSet<>();
		}
		for (URI id : predicate) {
			iriReference.add(id);
		}
	}
	
	public void addIgnoredProperty(URI... predicate) {
		if (ignoredProperty == null) {
			ignoredProperty = new HashSet<>();
		}
		for (URI id : predicate) {
			ignoredProperty.add(id);
		}
	}
	
	public boolean isIgnoredProperty(URI predicate) {
		return ignoredProperty!=null && ignoredProperty.contains(predicate);
	}
	
	public boolean isIriReference(URI predicate) {
		return iriReference!=null && iriReference.contains(predicate);
	}
	
	public void setLocalNameService(LocalNameService nameService) {
		this.nameService = nameService;
	}
	
	
	public ValueFactory getValueFactory() {
		return valueFactory;
	}


	public void put(Method method, URI predicate) {
		methodMap.put(method, predicate);
	}
	
	public void register(Class<?> javaClass) {
		if (!memory.contains(javaClass) && !skipClass.contains(javaClass)) {
			memory.add(javaClass);
			
			Method[] methodList = javaClass.getMethods();
			for (Method m : methodList) {
				getterPredicate(m);
			}
		}
	}
	
	
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	public LocalNameService getNameService() {
		return nameService;
	}

	public void setNameService(LocalNameService nameService) {
		this.nameService = nameService;
	}

	public URI getterPredicate(Method m) {
		if (m.getDeclaringClass() == Object.class) {
			return null;
		}
		
		
		String name = m.getName();
		if ("getId".equals(name)) {
			return null;
		}
		
		URI result = methodMap.get(m);
		if (result == NULL) {
			return null;
		}
		
		if (result == null) {

			if (name.startsWith("get")) {
				Class<?>[] paramList = m.getParameterTypes();
				if (paramList.length==0) {
					
					String resourceName = getterResourceName(name);
					
					Set<URI> nameList = nameService.lookupLocalName(resourceName);
					

					RdfProperty annotation = m.getAnnotation(RdfProperty.class);
					if (annotation != null) {
						String value = annotation.value();
						
						for (URI uri : nameList) {
							if (uri.stringValue().equals(value)) {
								if (isIgnoredProperty(uri)) {
									methodMap.put(m, NULL);
									return null;
								}
								methodMap.put(m, uri);
								return uri;
							}
						}
						result = new URIImpl(value);
						if (isIgnoredProperty(result)) {
							methodMap.put(m, NULL);
							return null;
						}
						// TODO: Should we store the mapping m -> result ?
						return result;
					}
					
					
					if (nameList.size() == 1) {
						URI resourceId = nameList.iterator().next();
						methodMap.put(m, resourceId);
						return resourceId;
					}
					if (nameList.isEmpty()) {
						ignoredMethod.put(m, NAME_NOT_FOUND);
					}
					
					if (!ignoredMethod.containsKey(m)) {
						ignoredMethod.put(m, AMBIGUOUS_NAME);
						if (logger.isWarnEnabled()) {
							logger.warn("Ambiguous predicate: {}", nameList.toString());
							
						}
					}
				}
			}

			methodMap.put(m, NULL);

		} 
		return result;
	}

	private String getterResourceName(String name) {
		StringBuilder builder = new StringBuilder();
		for (int i=3; i<name.length(); i++) {
			char c = name.charAt(i);
			if (i==3) {
				c = Character.toLowerCase(c);
			}
			builder.append(c);
		}
		return builder.toString();
	}


}
