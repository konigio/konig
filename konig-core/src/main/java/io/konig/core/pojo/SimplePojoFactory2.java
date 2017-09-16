package io.konig.core.pojo;

import java.lang.reflect.Constructor;

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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.annotation.InverseOf;
import io.konig.annotation.RdfList;
import io.konig.annotation.RdfProperty;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

public class SimplePojoFactory2 implements PojoFactory {

	private Map<String, ClassInfo<?>> classInfo = new HashMap<>();
	private PojoContext config;
	
	public SimplePojoFactory2() {
		config = new PojoContext();
	}
	
	public SimplePojoFactory2(PojoContext config) {
		this.config = config;
	}
	
	@Override
	public <T> T create(Vertex v, Class<T> type) throws KonigException {

		return doCreate(v, type);
	}


	@Override
	public void createAll(Graph graph) throws KonigException {
		
		doCreateAll(graph);
		
	}
	
		

		public void doCreateAll(Graph graph) {
			
			Set<Entry<Resource, Class<?>>> entries = config.getClassMap().entrySet();
			for (Entry<Resource,Class<?>> e : entries) {
				Resource owlClass = e.getKey();
				Class<?> javaClass = e.getValue();
				
				List<Vertex> list = graph.v(owlClass).in(RDF.TYPE).toVertexList();
				for (Vertex v : list) {
					doCreate(v, javaClass);
				}
				
			}
			
		}

		

		public <T> T doCreate(Vertex v, Class<T> type) throws KonigException {
			

			try {
				
				ClassInfo<T> info = getClassInfo(type);
				return create(v, info);
				
			} catch (
				InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException | NoSuchMethodException | SecurityException e
			) {
				throw new KonigException(e);
			}
		}

		@SuppressWarnings("unchecked")
		private <T> T create(Vertex v, ClassInfo<T> info) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			Resource resourceId = v.getId();
			Object instance = config.getIndividual(resourceId);
			
			if (instance!=null && info.getJavaType().isAssignableFrom(instance.getClass())) {
				return (T) instance;
			}
			
			if (instance!=null) {
				throw new KonigException("Type conflict for resource <" + resourceId + ">.  Expected " + 
						info.getJavaType().getName() + " but found " + instance.getClass().getName());
			}
			
			T pojo = null;
			
			Class<?> javaType = info.getJavaType();
			if (javaType == Resource.class) {
				return (T) v.getId();
			}
			pojo = info.getJavaType().newInstance();
			Set<Edge> edgeSet = v.outEdgeSet();
			info.setIdProperty(pojo, v);
			config.mapObject(resourceId, pojo);
			config.commitObject(resourceId, pojo);
			Graph g = v.getGraph();
			for (Edge e : edgeSet) {
				URI predicate = e.getPredicate();
				PropertyInfo p = info.getPropertyInfo(predicate);
				if (p != null) {
					Value value = e.getObject();
					p.set(this, g, pojo, value);
				}
			}
			
			return pojo;
		}

		<T> ClassInfo<T> getClassInfo(Class<T> javaType) {
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
						if (typeList.length==1 && Resource.class.isAssignableFrom(typeList[0])) {
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
					
					Method listSetter = null;
					for (Method m : methods) {
						String name = m.getName();
						
						if (setterName.equals(name) || adderName.equals(name)) {
							
							Class<?>[] typeList = m.getParameterTypes();
							if (typeList.length == 1) {
								Class<?> typeParam = typeList[0];
								
								if (List.class.isAssignableFrom(typeParam)) {
									listSetter = m;
								}
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
					
					if (result == null && listSetter!=null) {
						ListInfo listInfo = listInfo(predicate, listSetter);
						if (listInfo != null) {
							result = new PropertyInfo(predicate, listInfo);
						}
					}
					
					if (result != null) {
						add(result);
					}
				}
				
				return result;
			}
			
			private ListInfo listInfo(URI predicate, Method listSetter) {
				Type[] genericParamTypes = listSetter.getGenericParameterTypes();
				for (Type genericParamType : genericParamTypes) {
					if (genericParamType instanceof ParameterizedType) {
						ParameterizedType pType = (ParameterizedType) genericParamType;
						Type[] argTypes = pType.getActualTypeArguments();
						if (argTypes.length==1) {
							Class<?> argType = (Class<?>) argTypes[0];
							if (argType == String.class) {
								String getterName = BeanUtil.getterName(predicate);
								for (Method m : methods) {
									if (m.getName().equals(getterName) && m.getParameterTypes().length==0) {
										return new ListInfo(listSetter, m);
									}
								}
							}
						}
					}
				}
				return null;
			}

			private void add(PropertyInfo info) {
				propertyMap.put(info.getPredicate(), info);
			}
			
			
		}
		
		private class AdderInfo {
			Class<?> javaListContainer;
			PropertyInfo propertyInfo;
			
			public AdderInfo(Class<?> javaListContainer, PropertyInfo info) {
				this.javaListContainer = javaListContainer;
				this.propertyInfo = info;
			}
			
			
		}
		
		private static class ListInfo {
			private Method setter;
			private Method getter;
			
			
			
			public ListInfo(Method setter, Method getter) {
				this.setter = setter;
				this.getter = getter;
			}



			void set(SimplePojoFactory2 worker, Graph g, Object instance, Value value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
				List<String> list = (List<String>) getter.invoke(instance);
				if (list == null) {
					list = new ArrayList<>();
					setter.invoke(instance, list);
				}
				list.add(value.stringValue());
			}
		}
		
		private class PropertyInfo {
			private URI predicate;
			private Method setter;
			private Method valueToEnum;
			
			private AdderInfo adderInfo;
			private ListInfo listInfo;
			
			private Object factory;
			private Method factoryCreateMethod;
			private boolean noFactory;
			
			public PropertyInfo(URI predicate, ListInfo listInfo) {
				this.predicate = predicate;
				this.listInfo = listInfo;
			}
			
			public PropertyInfo(URI predicate, Method setter) {
				this.predicate = predicate;
				this.setter = setter;
				
				
				Class<?>[] paramTypes = setter.getParameterTypes();
				if (paramTypes.length == 1) {
					Class<?> listContainerType = paramTypes[0];
					RdfList annotation = listContainerType.getAnnotation(RdfList.class);
					if (annotation != null) {
						
						Method[] methodList = listContainerType.getMethods();
						for (Method method : methodList) {
							if (method.getName().equals("add") && method.getParameterTypes().length==1) {
								PropertyInfo listPropertyInfo = new PropertyInfo(predicate, method);
								adderInfo = new AdderInfo(listContainerType, listPropertyInfo);
								break;
							}
						}
						if (adderInfo == null) {
							throw new KonigException("'add' method not found on class with RdfList annotation: " 
									+ listContainerType.getSimpleName());
						}
					}
				}
				
			}

			public URI getPredicate() {
				return predicate;
			}

			void set(SimplePojoFactory2 worker, Graph g, Object instance, Value value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
						
				Vertex valueVertex = null;
				List<Value> valueList = null;
				if (value instanceof BNode) {
					valueVertex = g.getVertex((BNode)value);
					valueList = valueVertex.asList();
				}
				if (listInfo != null) {
					listInfo.set(worker, g, instance, value);
					return;
				}
				Class<?>[] typeArray = setter.getParameterTypes();
				if (typeArray.length==1) {
					Class<?> type = typeArray[0];
					
					if (type.isAssignableFrom(Value.class) && valueList==null) {
						setter.invoke(instance, value);
					} else if (type == String.class) {
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
						
						if (valueList != null) {
							
							if (adderInfo != null) {
								buildList(worker, g, instance, valueList);
							} else {
								for (Value v : valueList) {
									set(worker, g, instance, v);
								}
							}
							
							
						} else {

							if (valueVertex == null) {
								valueVertex = g.getVertex((Resource)value);
							}
						
							type = javaType(valueVertex, type);
							Object object = worker.doCreate(valueVertex, type);
							setter.invoke(instance, object);
							setInverse(worker, instance, object);
						}
						
						
					} else if (type== boolean.class) {
						Boolean booleanValue = Boolean.parseBoolean(value.stringValue());
						setter.invoke(instance, booleanValue);
					} else if (value instanceof Literal) {
						Constructor<?>[] ctorList = type.getConstructors();
						for (Constructor<?> ctor : ctorList) {
							Class<?>[] argList = ctor.getParameterTypes();
							if (argList.length==1 && argList[0] == String.class) {
								Object stringObject = ctor.newInstance(value.stringValue());
								setter.invoke(instance, stringObject);
							}
						}
						// TODO: check for factory class in same package.
						
						tryValueFactory(type, instance, (Literal) value);
					}
				}
				
				
			}

			private void tryValueFactory(Class<?> type, Object instance, Literal value) {
				
				if (noFactory) {
					return;
				}
				
				try {
					if (factory == null) {

						String packageName = type.getPackage().getName();
						String simpleName = type.getSimpleName();
						
						StringBuilder builder = new StringBuilder();
						builder.append(packageName);
						builder.append('.');
						builder.append(simpleName);
						builder.append("Factory");
						
						String factoryClassName = builder.toString();
						Class<?> factoryClass = Class.forName(factoryClassName);
						
						builder = new StringBuilder();
						builder.append("create");
						builder.append(simpleName);
						
						String createMethodName = builder.toString();
						
						factoryCreateMethod = factoryClass.getMethod(createMethodName, String.class);
						if (factoryCreateMethod.getReturnType() == type) {
							Constructor<?> ctor = factoryClass.getConstructor();
							factory = ctor.newInstance();
						} else {
							noFactory = true;
						}
					}
					
					
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					noFactory = true;
				}

				if (factory!=null && factoryCreateMethod!=null) {
					try {
						Object propertyValue  = factoryCreateMethod.invoke(factory, value.stringValue());
						setter.invoke(instance, propertyValue);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new KonigException(e);
					}
				}
				
			}

			private Class<?> javaType(Vertex v, Class<?> type) {
				Set<Edge> typeSet = v.outProperty(RDF.TYPE);
				for (Edge e : typeSet) {
					Value object = e.getObject();
					if (object instanceof Resource) {
						Resource typeId = (URI) object;
						
						Class<?> javaClass = config.getJavaClass(typeId);
						if (javaClass != null && type.isAssignableFrom(javaClass)) {
							type = javaClass;
						}
					}
				}
				return type;
			}

			private void buildList(SimplePojoFactory2 worker, Graph g, Object instance, List<Value> valueList) 
				throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
				
				Object listContainer = adderInfo.javaListContainer.newInstance();
				setter.invoke(instance, listContainer);
				for (Value value : valueList) {
					adderInfo.propertyInfo.set(worker, g, listContainer, value);
				}
				
			}

			/**
			 * If the predicate associated with this PropertyInfo has an inverse, then
			 * set the inverse.
			 * @param worker
			 * @param subject
			 * @param object
			 */
			private void setInverse(SimplePojoFactory2 worker, Object subject, Object object) {
				
				InverseOf annotation = setter.getAnnotation(InverseOf.class);
				
				if (annotation != null) {
					URI inverseId = new URIImpl(annotation.value());

					Class<?> objectType = object.getClass();
					ClassInfo<?> classInfo = worker.getClassInfo(objectType);
					
					PropertyInfo inverseInfo = classInfo.getPropertyInfo(inverseId);
					if (inverseInfo != null) {
						try {
							// TODO:  If the setter is an 'add' method (i.e. adds to a collection), then
							//  we need to check the collection to ensure that the relationship has not
							//  already been established.
							inverseInfo.setter.invoke(object, subject);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new KonigException(e);
						}
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
					throw new KonigException("Cannot convert value to enum because value is not a URI " + value.stringValue());
				}
			}
			
		

	}
}

	
	
