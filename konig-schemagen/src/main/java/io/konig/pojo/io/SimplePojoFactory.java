package io.konig.pojo.io;

/*
 * #%L
 * Konig Schema Generator
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.annotation.RdfProperty;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.schemagen.SchemaGeneratorException;

public class SimplePojoFactory implements PojoFactory {

	private Map<String, ClassInfo<?>> classInfo = new HashMap<>();
	
	@Override
	public <T> T create(Vertex v, Class<T> type) throws ParseException {
		

		try {
			
			ClassInfo<T> info = getClassInfo(type);
			return create(v, info);
			
		} catch (
			InstantiationException | IllegalAccessException | IllegalArgumentException | 
			InvocationTargetException | NoSuchMethodException | SecurityException e
		) {
			throw new ParseException(e);
		}
	}

	private <T> T create(Vertex v, ClassInfo<T> info) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		T pojo = null;
		pojo = info.getJavaType().newInstance();
		Set<Edge> edgeSet = v.outEdgeSet();
		info.setIdProperty(pojo, v);
		Graph g = v.getGraph();
		for (Edge e : edgeSet) {
			System.out.println(e.toString());
			URI predicate = e.getPredicate();
			PropertyInfo p = info.getPropertyInfo(predicate);
			if (p != null) {
				Value value = e.getObject();
				p.set(g, pojo, value);
			}
		}
		
		return pojo;
	}
	
	
	private <T> ClassInfo<T> getClassInfo(Class<T> javaType) {
		@SuppressWarnings("unchecked")
		ClassInfo<T> result = (ClassInfo<T>) classInfo.get(javaType.getName());
		
		if (result == null) {
			result = new ClassInfo<T>(javaType);
			classInfo.put(javaType.getName(), result);
		}
		return result;
	}
	
	private class ClassInfo<T> {
		private Class<T> javaType;
		private Method idSetter;
		private Method[] methods;
		private Map<URI, PropertyInfo> propertyMap = new HashMap<>();
		
		public ClassInfo(Class<T> javaType) {
			this.javaType = javaType;
			methods = javaType.getMethods();
			
			setIdMethod();
		}
		
		public void setIdProperty(Object instance, Vertex v) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (idSetter != null) {

				Resource id = v.getId();
				if (id instanceof URI) {
					idSetter.invoke(instance, id);
				}
			}
		}
		
		private void setIdMethod() {
			for (Method m : methods) {
				String name = m.getName();
				if ("setId".equals(name)) {
					Class<?>[] typeList = m.getParameterTypes();
					if (typeList.length==1 && URI.class==typeList[0]) {
						idSetter = m;
						break;
					}
				}
			}
			
		}

		public Class<T> getJavaType() {
			return javaType;
		}

		public PropertyInfo getPropertyInfo(URI predicate) {
			
			PropertyInfo result = propertyMap.get(predicate);
			if (result == null) {
				String setterName = BeanUtil.setterName(predicate);
				String adderName = BeanUtil.adderName(predicate);
				
				for (Method m : methods) {
					String name = m.getName();
					
					if (setterName.equals(name) || adderName.equals(name)) {
						
						Class<?>[] typeList = m.getParameterTypes();
						if (typeList.length == 1) {
							Class<?> typeParam = typeList[0];
							if (Collection.class.isAssignableFrom(typeParam)) {
								continue;
							}
						}
						
						result = new PropertyInfo(predicate, m);
						break;
					}
					RdfProperty annotation = m.getAnnotation(RdfProperty.class);
					if (
						annotation != null && 
						predicate.stringValue().equals(annotation.value()) &&
						m.getParameterTypes().length==1
					) {
						result = new PropertyInfo(predicate, m);
						break;
					}
				}
				
				if (result != null) {
					add(result);
				}
			}
			
			return result;
		}
		
		private void add(PropertyInfo info) {
			propertyMap.put(info.getPredicate(), info);
		}
		
		
	}
	
	
	private class PropertyInfo {
		private URI predicate;
		private Method setter;
		private Method valueToEnum;
		
		public PropertyInfo(URI predicate, Method setter) {
			this.predicate = predicate;
			this.setter = setter;
		}

		public URI getPredicate() {
			return predicate;
		}

		void set(Graph g, Object instance, Value value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
						
			Class<?>[] typeArray = setter.getParameterTypes();
			if (typeArray.length==1) {
				Class<?> type = typeArray[0];
				
				if (type == String.class) {
					setter.invoke(instance, value.stringValue());
				} else if (type.isEnum()) {
					Object enumValue = getEnumValue(type, value);
					setter.invoke(instance, enumValue);
				} else if ((type == int.class) || (type==Integer.class) && value instanceof Literal) {
					Literal literal = (Literal) value;
					setter.invoke(instance, new Integer(literal.intValue()));
					
				} else if ((type == float.class)) {
					Float floatValue = floatValue(value);
					setter.invoke(instance, floatValue);
					
				} else if (type == URI.class && value instanceof URI) {
					setter.invoke(instance,  (URI) value);
				} else if (value instanceof Resource) {
					Vertex vertex = g.vertex((Resource)value);
					
					Object object = create(vertex, type);
					setter.invoke(instance, object);
				} else if (type== boolean.class) {
					Boolean booleanValue = Boolean.parseBoolean(value.stringValue());
					setter.invoke(instance, booleanValue);
				}
			}
			
			
		}

		private Float floatValue(Value value) {
			String text = value.stringValue();
			return new Float(text);
		}

		private Object getEnumValue(Class<?> type, Value value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			
			if (value instanceof URI) {
				URI uriValue = (URI) value;
				if (valueToEnum == null) {
					valueToEnum = type.getMethod("fromURI", URI.class);
				}
				return valueToEnum.invoke(null, uriValue);
				
			} else {
				throw new SchemaGeneratorException("Cannot convert value to enum because value is not a URI " + value.stringValue());
			}
		}
		
	}
	

}
