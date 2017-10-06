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


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.BeanUtil;
import io.konig.core.pojo.PojoContext;

public class ResourceHandler implements ValueHandler {
	
	private Class<?> javaClass;
	private Method setter;
	
	private Constructor<?> stringConstructor;
	private Deserializer deserializer;
	

	public ResourceHandler(Class<?> javaClass, Method setter) {
		this.javaClass = javaClass;
		this.setter = setter;
	}

	@Override
	public void handleValue(PropertyInfo propertyInfo) throws KonigException {
		
		Object javaObject = javaObject(propertyInfo);
			
		if (javaObject != null) {
			Object container = propertyInfo.getContainer();
			try {
				setter.invoke(container, javaObject);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new KonigException(e);
			}
		}

	}

	private Object javaObject(PropertyInfo propertyInfo) throws KonigException {

		Object javaObject = null;
		
		Value object = propertyInfo.getObject();
		if (object instanceof Resource) {

			Resource objectId = (Resource) object;
			javaObject = javaObject(objectId, propertyInfo);
			
		} else {

			Constructor<?> constructor = stringConstructor();
			if (constructor != null) {
				try {
					javaObject = constructor.newInstance(object.stringValue());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new KonigException(e);
				}
			}
			if (javaObject == null) {
				Deserializer factory = deserializer();
				javaObject = factory.create(object.stringValue());
			}
		}
		return javaObject;
	}

	private Deserializer deserializer() throws KonigException {
		if (deserializer == null) {
			Class<?> factoryClass = BeanUtil.factoryClass(javaClass);
			Method createMethod = BeanUtil.createMethod(javaClass, factoryClass);
			Object factory = newInstance(factoryClass);
			deserializer = new Deserializer(factory, createMethod);
		}
		return deserializer;
	}

	private Object newInstance(Class<?> type) {
		if (type != null) {
			try {
				return type.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new KonigException(e);
			}
		}
		return null;
	}

	private Constructor<?> stringConstructor() {
		if (stringConstructor == null) {
			Constructor<?>[] ctorList = javaClass.getConstructors();
			for (Constructor<?> ctor : ctorList) {
				if (ctor.getParameterTypes().length==1 && String.class == ctor.getParameterTypes()[0]) {
					stringConstructor = ctor;
					break;
				}
			}
		}
		return stringConstructor;
	}

	private Object javaObject(Resource objectId, PropertyInfo propertyInfo) {
		PojoContext context = propertyInfo.getContext();
		
		if (javaClass == Resource.class) {
			return objectId;
		}
		
		if (javaClass == URI.class) {
			return objectId instanceof URI ? objectId : null;
		}
		
		if (javaClass == BNode.class) {
			return objectId instanceof BNode ? objectId : null;
		}
		
		Object result = context.getIndividual(objectId);
		if (result == null) {
		
			Vertex subject = propertyInfo.getSubject().getVertex();
			Graph graph = subject.getGraph();
			
			Vertex objectVertex = graph.getVertex(objectId);
	
			
			PojoHandler pojoHandler = context.getPojoHandler();
			
			PojoInfo pojoInfo = new PojoInfo();
			pojoInfo.setContext(context);
			pojoInfo.setExpectedJavaClass(javaClass);
			pojoInfo.setVertex(objectVertex);
			
			pojoHandler.buildPojo(pojoInfo);
			
			result = pojoInfo.getJavaObject();
		}
		return result;
	}
	
	private static class Deserializer {
		private Object factory;
		private Method createMethod;
		
		public Deserializer(Object factory, Method createMethod) {
			this.factory = factory;
			this.createMethod = createMethod;
		}
		
		public Object create(String value) throws KonigException {
			try {
				return createMethod==null ? null : createMethod.invoke(factory, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new KonigException(e);
			}
		}
		
	}

}
