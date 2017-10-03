package io.konig.core.pojo.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

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


import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.PojoCreator;

public class CreatorPojoHandler extends BasicPojoHandler {

	private Class<? extends PojoCreator<?>> creatorClass;
	private PojoCreator<?> creator;
	private Map<Class<?>, ValueHandler> elementHandlerMap;
	
	public CreatorPojoHandler(Class<?> javaClass, Class<? extends PojoCreator<?>> creatorClass) {
		super(javaClass);
		this.creatorClass = creatorClass;
	}

	@Override
	protected Object newInstance(PojoInfo pojoInfo) throws KonigException {
		try {
			Vertex v = pojoInfo.getVertex();
			if (creator == null) {
				creator = creatorClass.newInstance();
			}
			Object pojo = creator.create(v);
			
			if (v.isList()) {
				handleList(pojo, pojoInfo);
			}
			
			return pojo;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException  e) {
			throw new KonigException(e);
		}
	}

	private void handleList(Object pojo, PojoInfo pojoInfo) {
		pojoInfo.setJavaObject(pojo);
		Vertex v = pojoInfo.getVertex();
		
		PropertyInfo propertyInfo = new PropertyInfo();
		propertyInfo.setPredicate(RDF.FIRST);
		propertyInfo.setSubject(pojoInfo);
		
		ValueHandler elementHandler = elementHandler(pojo);
		List<Value> valueList = v.asList();
		
		for (Value value : valueList) {
			propertyInfo.setObject(value);
			elementHandler.handleValue(propertyInfo);
		}
		
	}

	private ValueHandler elementHandler(Object pojo) {
		
		Class<?> pojoType = pojo.getClass();
		if (elementHandlerMap == null) {
			elementHandlerMap = new HashMap<>();
		}
		ValueHandler handler = elementHandlerMap.get(pojoType);
		if (handler == null) {
			Method[] methodList = pojoType.getMethods();
			for (Method m : methodList) {
				if (m.getName().equals("add")) {
					Class<?>[] paramTypes = m.getParameterTypes();
					if (paramTypes.length==1) {
						
						Type genericSuperType = pojoType.getGenericSuperclass();
						ParameterizedType pType = (ParameterizedType) genericSuperType;
						Type[] parameterArgTypes = pType.getActualTypeArguments();
						
						
						
						Class<?> argType = (Class<?>) parameterArgTypes[0];
						handler = valueHandler(argType, m);
						elementHandlerMap.put(pojoType, handler);
						break;
					}
				}
			}
		}
		return handler;
	}

}
