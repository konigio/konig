package io.konig.core.pojo.impl;

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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.annotation.RdfList;
import io.konig.annotation.RdfProperty;
import io.konig.core.Edge;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.BeanUtil;
import io.konig.core.util.StringUtil;

public class BasicPojoHandler implements PojoHandler {

	private Class<?> javaClass;
	private Map<String, ValueHandler> propertyHandlerMap = new HashMap<>();
	private Method idMethod;
	

	public BasicPojoHandler(Class<?> javaClass) {
		this.javaClass = javaClass;
		buildPropertyHandlers();
	}


	private void buildPropertyHandlers() {
		Method[] methodList = javaClass.getMethods();
		
		for (Method m : methodList) {
			if (m.getParameterTypes().length==1) {
				String key = null;
				RdfProperty note = m.getAnnotation(RdfProperty.class);
				if (note != null) {
					key = StringUtil.rdfLocalName(note.value());
					if (key == null) {
						String methodName = m.getName();
						throw new KonigException("Invalid URI for RdfProperty of " + methodName);
					} 
				} else {
					key = BeanUtil.setterKey(m);
				}
				if (key != null) {
					if ("id".equals(key) && m.getParameterTypes()[0].isAssignableFrom(URI.class)) {
						idMethod = m;
					} else {
						putMethod(key, m);
					}
				}
			}
		}
		
	}


	private void putMethod(String key, Method m) {
		
		Class<?> type = m.getParameterTypes()[0];
		ValueHandler handler = valueHandler(type, m);
		
		if (m.getName().startsWith("appendTo")) {
			handler = new AppendToListHandler(handler);
		}
		
		ValueHandler prior = propertyHandlerMap.get(key);
		if (prior != null) {
			handler = chooseBestHandler(prior, handler);
		} 
		propertyHandlerMap.put(key, handler);
		
	}


	private ValueHandler chooseBestHandler(ValueHandler prior, ValueHandler handler) {
		
		
		
		if (prior instanceof MultiValueHandler) {
			return prior;
		}
		
		if (handler instanceof MultiValueHandler) {
			return handler;
		}
		
		if (prior instanceof ResourceHandler) {
			return prior;
		}
		
		if (handler instanceof ResourceHandler) {
			return handler;
		}
		
		return prior;
		
	}


	protected ValueHandler valueHandler(Class<?> type, Method m) {
		ValueHandler result = null;
		if (type == String.class) {
			result = new StringValueHandler(m);
		} else if (isCollectionType(type)) {
			result = collectionHandler(m);
		} else if (type == float.class || type==Float.class) {
			result = new FloatValueHandler(m);
		} else if (type == URI.class) {
			result = new UriValueHandler(m);
		} else if (type.isEnum()) {
			result = enumValueHandler(type, m);
		} else {
			result = new ResourceHandler(type, m);
		}
		return result;
	}

	private EnumValueHandler enumValueHandler(Class<?> type, Method m) {
		
		try {
			Method valueToEnum = type.getMethod("fromURI", URI.class);
			return new EnumValueHandler(m, valueToEnum);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new KonigException(e);
		}
	}

	private ValueHandler collectionHandler(Method collectionSetter) throws KonigException {
		Class<?> collectionType = collectionSetter.getParameterTypes()[0];
		if (collectionType.isInterface()) {
			if (List.class.isAssignableFrom(collectionType)) {
				collectionType = ArrayList.class;
			} else if (Set.class.isAssignableFrom(collectionType)) {
				collectionType = HashSet.class;
			} else {
				throw new KonigException("Unspported Collection argument for method: " + collectionSetter);
			}
		} else if (Modifier.isAbstract(collectionType.getModifiers())) {
			throw new KonigException("Abstract collection type not supported on method: " + collectionSetter);
		}
		
		ValueHandler elementHandler = elementHandler(collectionType, collectionSetter);
		RdfListValueHandler rdfListHandler = new RdfListValueHandler(collectionType, collectionSetter, null, elementHandler);

		if (collectionType.getAnnotation(RdfList.class) != null) {
			return rdfListHandler;
		}
		CollectionValueHandler collectionHandler = new CollectionValueHandler(collectionType, collectionSetter, null, elementHandler);
		return new MultiValueHandler(collectionHandler, rdfListHandler);
	}


	private ValueHandler elementHandler(Class<?> collectionType, Method collectionSetter) {
		Method adderMethod = adderMethod(collectionType);
		if (adderMethod == null) {
			throw new KonigException("adder not found for collection set via " + collectionSetter);
		}
		
		Class<?> argType = null;
		Type genericParamType = collectionSetter.getGenericParameterTypes()[0];
		if (genericParamType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) genericParamType;
			Type[] argTypes = pType.getActualTypeArguments();
			if (argTypes.length==1) {
				argType = (Class<?>) argTypes[0];
				
			}
		} else {
			RdfList rdfList = collectionType.getAnnotation(RdfList.class);
			if (rdfList != null) {
				argType = adderMethod.getParameterTypes()[0];
				
			}
		}
		
		if (argType != null) {
			ValueHandler valueHandler = valueHandler(argType, adderMethod);
			if (valueHandler != null) {
				return valueHandler;
			}
		}
		
		throw new KonigException("Adder argument not found for collection set via " + collectionSetter);
	}


	private Method adderMethod(Class<?> collectionType) {

		Method[] methodList = collectionType.getMethods();
		for (Method m : methodList) {
			if (m.getName().equals("add") && m.getParameterTypes().length==1) {
				return m;
			}
		}
		throw new KonigException("'add' method not found on collection type: " + collectionType);
	}


	private boolean isCollectionType(Class<?> type) {
		if (Collection.class.isAssignableFrom(type) || type.getAnnotation(RdfList.class)!=null) {
			return true;
		}
		return false;
	}

	@Override
	public void buildPojo(PojoInfo pojoInfo) throws KonigException {
		
		Object pojo = newInstance(pojoInfo);
		pojoInfo.setJavaObject(pojo);
		setProperties(pojoInfo);
	}
	
	
	protected Object newInstance(PojoInfo pojoInfo) throws KonigException {
		try {
			return javaClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new KonigException(e);
		}
	}


	private void setProperties(PojoInfo pojoInfo) throws KonigException {
		Vertex v = pojoInfo.getVertex();
		Set<Entry<URI,Set<Edge>>> outEdges = v.outEdges();

		setId(pojoInfo);
		
		if (!outEdges.isEmpty()) {

			PropertyInfo propertyInfo = new PropertyInfo();
			propertyInfo.setSubject(pojoInfo);
			
			for (Entry<URI,Set<Edge>> entry : v.outEdges()) {
				URI predicate = entry.getKey();
				propertyInfo.setPredicate(predicate);
				propertyInfo.setObject(null);
				

				Set<Edge> edgeSet = entry.getValue();
				if (!edgeSet.isEmpty()) {
					ValueHandler edgeHandler = propertyHandler(propertyInfo);
					
					if (edgeHandler != null) {

						ThreePhaseValueHandler phaseHandler = edgeHandler instanceof ThreePhaseValueHandler ?
								(ThreePhaseValueHandler)edgeHandler : null;
						if (phaseHandler != null) {
							phaseHandler.setUp(propertyInfo);
						}
						for (Edge edge : edgeSet) {
							Value object = edge.getObject();
							propertyInfo.setObject(object);
							edgeHandler.handleValue(propertyInfo);
						}
						if (phaseHandler != null) {
							phaseHandler.tearDown(propertyInfo);
						}
					}
				}
			}
		}
	}

	


	private void setId(PojoInfo pojoInfo) throws KonigException {
		
		Vertex v = pojoInfo.getVertex();
		Resource id = v.getId();
		if (id instanceof URI && idMethod != null) {
			URI uri = (URI) id;
			Object javaObject = pojoInfo.getJavaObject();
			try {
				idMethod.invoke(javaObject, uri);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new KonigException(e);
			}
		}
		
	}


	private ValueHandler propertyHandler(PropertyInfo propertyInfo) {
		URI predicate = propertyInfo.getPredicate();
		String localName = predicate.getLocalName();
		return propertyHandlerMap.get(localName.toLowerCase());
	}



}
