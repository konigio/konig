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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.annotation.RdfList;
import io.konig.annotation.RdfProperty;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SimplePojoEmitter implements PojoEmitter {
	private static SimplePojoEmitter INSTANCE = new SimplePojoEmitter();
	
	private ValueFactory valueFactory = ValueFactoryImpl.getInstance();


	public static SimplePojoEmitter getInstance() {
		return INSTANCE;
	}
	@Override
	public void emit(EmitContext context, Object pojo, Graph sink)  {
		
		Worker worker = new Worker(context, sink);
		worker.emit(pojo);

	}


	@Override
	public void emit(NamespaceManager nsManager, Shape shape, Object pojo, Graph sink) {
		Worker worker = new Worker(nsManager, sink);
		worker.emit(shape, pojo);
		
	}
	
	private class Worker {
		private EmitContext context;
		private Map<Object, Resource> pojoIdMap = new HashMap<>();
		private Set<Object> memory = new HashSet<>();
		private Graph sink;
		private NamespaceManager sinkNamespaces;
		private NamespaceManager sourceNamespaces;
		

		public Worker(EmitContext context, Graph sink) {
			this.context = context;
			this.sink = sink;
			sinkNamespaces = sink.getNamespaceManager();
			sourceNamespaces = context.getNamespaceManager();
		}
		
		public Worker(NamespaceManager sourceNamespaces, Graph sink) {
			this.sink = sink;
			this.sourceNamespaces = sourceNamespaces;
			this.sinkNamespaces = sink.getNamespaceManager();
			
		}
		
		public void emit(Shape shape, Object pojo) {
			
			if (!memory.contains(pojo)) {
				memory.add(pojo);
				Resource subject = getId(pojo);
				emitProperties(subject, shape, pojo);
			}
			
		}

		private void emitProperties(Resource subject, Shape shape, Object pojo) {
			List<PropertyConstraint> propertyList = shape.getProperty();
			for (PropertyConstraint p : propertyList) {
				URI predicate = p.getPredicate();
				if (predicate != null) {

					Method getter = getter(p, pojo);
					if (getter != null) {
						
						try {
							Object value = getter.invoke(pojo);
							
							if (value instanceof Collection) {
								emitCollection(p, subject, predicate, (Collection<?>)value);
							} else if (value != null) {

								Value object = toValue(value);
								sink.edge(subject, predicate, object);
								
								Shape valueShape = p.getShape();
								if (valueShape != null) {
									emit(valueShape, value);
								}
							}
							
							
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							
							throw new KonigException(e);
						}
						
					} else {
						
						String predicateName = predicate.getLocalName();
						String message = "On class" + pojo.getClass().getSimpleName() + 
								", getter not found for predicate: " + predicateName;
						throw new KonigException(message);
					}
				}
			}
		}

		private void emitCollection(PropertyConstraint p, Resource subject, URI predicate, Collection<?> collection) {
			for (Object value : collection) {
				Value object = toValue(value);
				sink.edge(subject, predicate, object);
				Shape valueShape = p.getShape();
				if (valueShape != null) {
					emit(valueShape, value);
				}
			}
			
		}

		private Method getter(PropertyConstraint p, Object pojo) {
			URI predicate = p.getPredicate();
			if (predicate != null) {
				Class<?> javaClass = pojo.getClass();
				String getterName = BeanUtil.getterName(predicate);
				Method[] methodList = javaClass.getMethods();
				
				for (Method m : methodList) {
					RdfProperty annotation = m.getAnnotation(RdfProperty.class);
					if (annotation != null) {
						String rdfProperty = annotation.value();
						if (rdfProperty.equals(predicate.stringValue())) {
							return m;
						}
					}
					
					String methodName = m.getName();
					if (methodName.equals(getterName)) {
						return m;
					}
				}
			}
			
			
			return null;
		}

		void emit(Object pojo)  {
			
			doEmit(null, pojo);
			
		}
		
		void doEmit(Resource id, Object pojo)  {
			
			if (!memory.contains(pojo)) {
				context.register(pojo.getClass());
				if (id == null) {
					id = getId(pojo);
				}
				if (id != null) {
					memory.add(pojo);
					emitProperties(id, pojo);
				}
			}

			
		}

		private void emitProperties(Resource subject, Object pojo)  {
			
			
			Class<?> javaClass = pojo.getClass();
			Method[] methodList = javaClass.getMethods();
			for (Method m : methodList) {
				URI predicate = context.getterPredicate(m);
				if (predicate != null) {
					Object javaObject;
					try {
						javaObject = m.invoke(pojo);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new KonigException(e);
					}
					
					if (javaObject instanceof Collection) {
						emitCollection(subject, predicate, (Collection<?>) javaObject);
					} else {
						Value object = toValue(javaObject);
						if (object != null) {
							registerNamespace(predicate);
							
							sink.edge(subject, predicate, object);
						}
						
						if (object instanceof Resource && !context.isIriReference(predicate)) {
							doEmit((Resource)object, javaObject);
						}
					}
					
					
				}
			}
			
		}
		
		private void emitCollection(Resource subject, URI predicate, Collection<?> javaObject) {
			
			Class<?> javaClass = javaObject.getClass();
			RdfList listNote = javaClass.getAnnotation(RdfList.class);
			
			if (listNote != null) {
				emitRdfList(subject,  predicate, javaObject);
			} else {

				boolean registered = false;
				for (Object value : javaObject) {
					Value object = toValue(value);
					if (object != null) {
						if (!registered) {
							registerNamespace(predicate);
							registered = true;
						}
						sink.edge(subject, predicate, object);
						
						if (object instanceof Resource) {
							doEmit((Resource)object, value);
						}
					}
				}
			}
			
		}

		private void emitRdfList(Resource subject, URI predicate, Collection<?> javaCollection) {
		
			if (!javaCollection.isEmpty()) {
			
				Vertex priorList = null;
				
				for (Object value : javaCollection) {
					
					Vertex list = sink.vertex();
					Value object = toValue(value);
					sink.edge(list.getId(), RDF.FIRST, object);
					
					if (priorList == null) {
						registerNamespace(predicate);
						sink.edge(subject, predicate, list.getId());
					} else {
						sink.edge(priorList.getId(), RDF.REST, list.getId());
					}
					
					priorList = list;
				}
				
				sink.edge(priorList.getId(), RDF.REST, RDF.NIL); 
			}
			
		}

		private Value toValue(Object object) {
			
			Value value = BeanUtil.toValue(object);
			if (value != null) {
				if (value instanceof URI) {
					registerNamespace((URI)value);
				}
				return value;
			}
			
			return getId(object);
		}


		private Resource getId(Object pojo) {
			if (pojo == null) {
				return null;
			}
			
			Resource result = pojoIdMap.get(pojo);
			if (result == null) {
				Class<?> javaClass = pojo.getClass();
				try {
					Method idMethod = javaClass.getMethod("getId");
					Class<?> returnType = idMethod.getReturnType();
					if (Resource.class.isAssignableFrom(returnType)) {
						
						result = (Resource) idMethod.invoke(pojo);
						if (result != null) {
							if (result instanceof URI) {
								URI uri = (URI) result;
								registerNamespace(uri);
							}
						}
					}
				
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
					throw new KonigException(e);
				} catch (NoSuchMethodException e) {
					// Ignore
				}
			}
			
			if (result == null) {
				result = valueFactory.createBNode();
			}
			pojoIdMap.put(pojo, result);
			
			return result;
		}

		private void registerNamespace(URI uri) {
			
			if (sinkNamespaces != null && sourceNamespaces!=null) {
				
				String namespace = uri.getNamespace();
				Namespace ns = sourceNamespaces.findByName(namespace);
				if (ns != null) {
					sinkNamespaces.add(ns);
				}
			}
		}

		
	}

}
