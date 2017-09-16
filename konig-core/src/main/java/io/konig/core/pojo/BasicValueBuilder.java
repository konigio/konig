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


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.annotation.RdfList;
import io.konig.annotation.RdfProperty;
import io.konig.core.KonigException;
import io.konig.core.util.StringUtil;

public class BasicValueBuilder implements CollectionBuilder {

	private Class<?> javaClass;
	private Map<String,ValueBuilder> predicateMap = new HashMap<>();
	
	public BasicValueBuilder(Class<?> javaClass) {
		this.javaClass = javaClass;
		buildMap(javaClass);
	}

	private void buildMap(Class<?> javaClass) {
		
		Method[] methodList = javaClass.getMethods();
		for (Method m : methodList) {
		
			if (m.getParameterTypes().length==1) {
				String key = null;
				String methodName = m.getName();
				RdfProperty note = m.getAnnotation(RdfProperty.class);
				if (note != null) {
					key = StringUtil.rdfLocalName(note.value());
					if (key == null) {
						throw new KonigException("Invalid URI for RdfProperty of " + methodName + ": " + note.value());
					}
				} else {
					key = BeanUtil.setterKey(m);
				}
				if (key != null) {
					putMethod(key, m);
				}
			}
			
		}
		
	}

	private void putMethod(String key, Method m) {
		
		Class<?> type = m.getParameterTypes()[0];
		
		ValueBuilder pb = propertyBuilder(type, m);
		
		ValueBuilder prior = predicateMap.get(key);
		if (prior != null) {
			pb = combine(prior, pb);
		}
		predicateMap.put(key, pb);
		
	}


	private ValueBuilder resourceSetter(Class<?> argType, Method m) {
		return new ResourceSetter(m, argType);
	}

	private ValueBuilder collectionSetter(Method m) {
		Class<?> collectionType = m.getParameterTypes()[0];
		if (collectionType.isInterface()) {
			if (List.class.isAssignableFrom(collectionType)) {
				collectionType = ArrayList.class;
			} else if (Set.class.isAssignableFrom(collectionType)) {
				collectionType = HashSet.class;
			} else  {
				throw new KonigException("Unsupported Collection for method: " + m);
			}
		} else if (Modifier.isAbstract(collectionType.getModifiers())) {
			throw new KonigException("Abstract collection type not supported on method: " + m);
		}
		
		ValueBuilder adder = adder(collectionType, m);
		
		return new CollectionSetter(collectionType, m, adder);
	}

	private ValueBuilder adder(Class<?> collectionType, Method collectionSetter) throws KonigException {
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
			ValueBuilder propertyBuilder = propertyBuilder(argType, adderMethod);
			if (propertyBuilder != null) {
				return propertyBuilder;
			}
		}
		
		throw new KonigException("Adder argument not found for collection set via " + collectionSetter);
	}

	private ValueBuilder propertyBuilder(Class<?> type, Method m) {
		ValueBuilder pb = null;
		if (Value.class.isAssignableFrom(type)) {
			pb = new ValueSetter(m);
			
		} else if (type == String.class) {
			pb = new StringSetter(m);
			
		} else if (type == int.class) {
			pb = new IntSetter(m);
		} else if (type == float.class) {
			pb = new FloatSetter(m);
			
		} else if (type.isEnum()) {
			pb = enumSetter(type, m);
		} else if (type.isAssignableFrom(Calendar.class)) {
			pb = new CalendarSetter(m);
		} else if (isCollectionType(type)) {
			pb = collectionSetter(m);
		} else {
			pb = resourceSetter(type, m);
		}
		return pb;
	}

	private boolean isCollectionType(Class<?> type) {
		if (Collection.class.isAssignableFrom(type)) {
			return true;
		}
		RdfList rdfList = type.getAnnotation(RdfList.class);
		if (rdfList != null) {
			return true;
		}
		return false;
	}

	private Method adderMethod(Class<?> collectionType) throws KonigException {
		Method[] methodList = collectionType.getMethods();
		for (Method m : methodList) {
			if (m.getName().equals("add") && m.getParameterTypes().length==1) {
				return m;
			}
		}
		throw new KonigException("'add' method not found on collection type: " + collectionType);
	}

	private ValueBuilder combine(ValueBuilder a, ValueBuilder b) {
	
		if (b == null) {
			return a;
		}
		if (a instanceof CollectionSetter && !(b instanceof CollectionSetter)) {
			return b;
		}
		return a;
	}

	private EnumSetter enumSetter(Class<?> type, Method m) {
		
		try {
			Method valueToEnum = type.getMethod("fromURI", URI.class);
			return new EnumSetter(m, valueToEnum);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new KonigException(e);
		}
	}

	@Override
	public void beginValue(ValueExchange exchange) throws KonigException {
		
		ValueBuilder builder = exchange.getValueBuilder();
		if (builder == this) {
			builder = getPropertyBuilder(exchange.getPredicate());
		}
		if (builder != null) {
			builder.beginValue(exchange);
		}
		
	}

	private ValueBuilder getPropertyBuilder(URI predicate) {
		String key = predicate==null ? "" : predicate.getLocalName().toLowerCase();
		
		return predicateMap.get(key);
	}

	@Override
	public void endValue(ValueExchange exchange) throws KonigException {
		
		ValueBuilder builder = exchange.getValueBuilder();
		if (builder != null && builder!=this) {
//			System.out.println("BasicPropertyBuilder.endProperty: " + builder);
			builder.endValue(exchange);
		} 
		
	}

	@Override
	public ValueExchange beginCollection(ValueExchange exchange) {
		URI predicate = exchange.getPredicate();
		ValueBuilder vb = getPropertyBuilder(predicate);
		exchange.setValueBuilder(vb);
		if (vb instanceof CollectionBuilder) {
			exchange = ((CollectionBuilder) vb).beginCollection(exchange);
		}
		return exchange;
	}

	@Override
	public ValueExchange endCollection(ValueExchange exchange) {

		ValueBuilder vb = exchange.getValueBuilder();
		if (vb instanceof CollectionBuilder && vb!=this) {
			exchange = ((CollectionBuilder) vb).endCollection(exchange);
		}
		return exchange;
	}

	public Class<?> getJavaClass() {
		return javaClass;
	}


}
