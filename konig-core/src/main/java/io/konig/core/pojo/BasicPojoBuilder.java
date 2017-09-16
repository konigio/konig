package io.konig.core.pojo;

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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

public class BasicPojoBuilder implements PojoBuilder {

	private Map<Class<?>,ValueBuilder> vbMap = new HashMap<>();
	
	public BasicPojoBuilder() {
		
	}

	@Override
	public void beginPojo(PojoExchange exchange) throws KonigException {
		
		try {

			Class<?> type = exchange.getJavaClass();
			PojoContext context = exchange.getContext();
			Vertex v = exchange.getVertex();
			Object pojo = null;
			if (v != null) {
				pojo = context.getIndividual(v.getId());
				if (pojo != null && type==null) {
					type = pojo.getClass();
				}
			}
			
			type = getType(type, v, context);
			
			if (type == null) {
				String vid = v==null ? "null" : v.getId().stringValue();
				exchange.setErrorMessage("Java class not found for resource: " + vid);
				return;
			}
			ValueBuilder vb = vbMap.get(type);
			if (vb == null) {
				vb = new BasicValueBuilder(type);
				vbMap.put(type, vb);
			}
			if (pojo == null) {
				pojo = newInstance(type, exchange);
				if (v != null) {
					context.mapObject(v.getId(), pojo);
				}
			}
			exchange.setJavaClass(type);
			exchange.setPojo(pojo);
			exchange.setValueBuilder(vb);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new KonigException(e);
		}

	}

	private Class<?> getType(Class<?> best, Vertex v, PojoContext context) {
		if (v != null) {
			Set<Edge> typeSet = v.outProperty(RDF.TYPE);
			for (Edge e : typeSet) {
				Value object = e.getObject();
				if (object instanceof URI) {
					URI typeId = (URI) object;
					Class<?> type = context.getJavaClass(typeId);
					if (type!=null && (best == null || best.isAssignableFrom(type))) {
						best = type;
					}
				}
			}
		}
		return best;
	}

	private Object newInstance(Class<?> type, PojoExchange exchange) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Literal value = exchange.getValue();
		if (value != null) {
			return newInstanceFromLiteral(exchange, type, value);
			
		}
		return type.newInstance();
	}

	

	private Object newInstanceFromLiteral(PojoExchange exchange, Class<?> type, Literal value) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		PojoContext context = exchange.getContext();
		
		PojoDeserializer creator = context.getDeserializer(type);
		if (creator != null) {
			return creator.deserialize(value.stringValue());
		}
		
		if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
		
			Constructor<?>[] ctorList = type.getConstructors();
			for (Constructor<?> ctor : ctorList) {
				Class<?>[] paramList = ctor.getParameterTypes();
				if (paramList.length == 1) {
					Class<?> paramType = paramList[0];
					if (paramType == String.class) {
						creator = new ConstructorDeserializer(ctor);
						break;
					}
				}
			}
		}
		
		if (creator == null) {
			Class<?> factoryClass = BeanUtil.factoryClass(type);
			if (factoryClass != null) {
				String methodName = "create" + type.getSimpleName();
				for (Method method : factoryClass.getMethods()) {
					Class<?>[] argList = method.getParameterTypes();
					if (argList.length==1 && argList[0]==String.class && method.getName().equals(methodName)) {
						try {
							Object factory = factoryClass.newInstance();
							creator = new FactoryBasedDeserializer(factory, method);
							break;
						} catch (Throwable ignore) {
							
						}
					}
				}
			}
		}
		if (creator != null) {
			context.putDeserializer(type, creator);
			return creator.deserialize(value.stringValue());
		}
		
		throw new KonigException("Cannot create type from literal: " + type);
	}

	@Override
	public ValueBuilder getValueBuilder(ValueExchange exchange) {
		return exchange.getPojoExchange().getValueBuilder();
	}

	@Override
	public void endPojo(PojoExchange exchange) {
		
		Object pojo = exchange.getPojo();
		Vertex v = exchange.getVertex();
		if (v != null && pojo != null) {
			exchange.getContext().commitObject(v.getId(), pojo);
		}

	}

}
