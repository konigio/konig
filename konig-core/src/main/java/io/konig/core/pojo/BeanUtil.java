package io.konig.core.pojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Calendar;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openrdf.model.Literal;

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


import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.KonigException;
import io.konig.core.util.StringUtil;

public class BeanUtil {

	private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
	
	public static String setterName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		String localName = predicate.getLocalName();
		if (localName.startsWith("is") && localName.length()>2 && Character.isUpperCase(localName.charAt(2))) {
			localName = localName.substring(2);
		}
		builder.append("set");
		builder.append(StringUtil.capitalize(localName));
		
		return builder.toString();
	}

	
	public static Class<?> creatorClass(Class<?> type) {
		String packageName = type.getPackage().getName();
		String simpleName = type.getSimpleName();
		StringBuilder builder = new StringBuilder();
		builder.append(packageName);
		builder.append('.');
		builder.append(simpleName);
		builder.append("Creator");
		
		String factoryName = builder.toString();
		
		Class<?> factoryClass = null;
		
		try {
			factoryClass = Class.forName(factoryName);
		} catch (Throwable ignore) {
			
		}
		
		return factoryClass;
	}
	
	public static Method createMethod(Class<?> type, Class<?> factoryClass) {
		if (type != null && factoryClass != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("create");
			builder.append(type.getSimpleName());
			String name = builder.toString();
			
			Method[] list = factoryClass.getMethods();
			for (Method m : list) {
				if (m.getName().equals(name)) {
					Class<?>[] paramList = m.getParameterTypes();
					if (paramList.length==1 && paramList[0]==String.class) {
						return m;
					}
				}
			}
		}
		
		return null;
	}
	
	public static Class<?> factoryClass(Class<?> type) {
		if (type == null) {
			return null;
		}
		Package pkg = type.getPackage();
		if (pkg == null) {
			return null;
		}
		String packageName = pkg.getName();
		String simpleName = type.getSimpleName();
		StringBuilder builder = new StringBuilder();
		builder.append(packageName);
		builder.append('.');
		builder.append(simpleName);
		builder.append("Factory");
		
		String factoryName = builder.toString();
		
		Class<?> factoryClass = null;
		
		try {
			factoryClass = Class.forName(factoryName);
		} catch (Throwable ignore) {
			
		}
		
		return factoryClass;
	}
	
	public static String setterKey(Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		
		if (paramTypes.length == 1) {

			String name = method.getName();
			if ("add".equals(name)) {
				return "";
			}
			if ((name.startsWith("set") || name.startsWith("add")) && 
				name.length()>3 && 
				Character.isUpperCase(name.codePointAt(3))
			) {
				String localName = name.substring(3);
				return localName.toLowerCase();
			}
			if (name.startsWith("appendTo") && name.length()>8 && Character.isUpperCase(name.codePointAt(8))) {
				String localName = name.substring(8);
				return localName.toLowerCase();
				
			}
		}
		
		return null;
	}
	
	
	
	public static String adderName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		builder.append("add");
		builder.append(StringUtil.capitalize(predicate.getLocalName()));
		
		return builder.toString();
	}
	
	public static String getterName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		String localName = predicate.getLocalName();
		builder.append("get");
		builder.append(StringUtil.capitalize(localName));
		
		return builder.toString();
	}
	
	public static Object toJavaObject(Literal literal) {
		URI datatype = literal.getDatatype();
		if (XMLSchema.INT.equals(datatype)) {
			return new Integer(literal.intValue());
		}
		if (XMLSchema.INTEGER.equals(datatype)) {
			return new Long(literal.longValue());
		}
		if (XMLSchema.LONG.equals(datatype)) {
			return new Long(literal.longValue());
		}
		if (datatype!=null && XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
			return literal.stringValue();
		}
		
		throw new KonigException("Unsupported datatype: " + datatype);
	}
	
	
	public static Value toValue(Object object)  {
		if (object == null) {
			return null;
		}
		ValueFactory valueFactory = ValueFactoryImpl.getInstance();

		if (object instanceof URI) {
			URI uri = (URI) object;
			return uri;
		}
		if (object instanceof String) {
			return valueFactory.createLiteral(object.toString());
		}
		if (object instanceof Integer) {
			return valueFactory.createLiteral((Integer)object);
		}
		if (object instanceof Long) {
			return valueFactory.createLiteral((Long)object);
		}
		if (object instanceof Calendar) {
			Calendar calendar = (Calendar) object;
			String value = dateTimeFormatter.print(calendar.getTimeInMillis());
			return valueFactory.createLiteral(value, XMLSchema.DATETIME);
		}
		
		if (object instanceof Enum) {
			try {
				Method getURI = object.getClass().getMethod("getURI");
				if (URI.class.isAssignableFrom(getURI.getReturnType())) {
					return (URI) getURI.invoke(object);
				}
			} catch (Throwable e) {
				// Ignore
			}
		}
		
		Method[] methodList = object.getClass().getMethods();
		for (Method m : methodList) {
			if ("toValue".equals(m.getName()) 
					&& m.getParameterTypes().length==0 
					&& Value.class.isAssignableFrom(m.getReturnType())
				) {
				try {
					return (Value) m.invoke(object);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new KonigException("Failed to get value", e);
				}
			}
		}
		
		return null;
	}

}
