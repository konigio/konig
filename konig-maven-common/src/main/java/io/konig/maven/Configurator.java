package io.konig.maven;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.konig.maven.Configurator.Element.Type;



public class Configurator {
	
	public static final String DEV_NULL = "/dev/null";
	
	private Properties globalProperties;
	private Map<String, Object> map = new HashMap<>();
	
	public Configurator(Properties globalProperties) {
		this.globalProperties = globalProperties;
	}
	
	public static File checkNull(File input) {
		return (input==null || input.toString().equals(DEV_NULL)) ?
			null : input;
	}


	public void configure(Object entity) throws ConfigurationException {
		Class<?> type = entity.getClass();
		List<Field> fieldList = new ArrayList<>();
		addAllFields(fieldList, type);
		
		injectSimpleDefaults(fieldList, entity);
		injectObjects(fieldList, entity);
		
	}


	private void injectObjects(List<Field> fieldList, Object entity) throws ConfigurationException {
		
		for (Field field : fieldList) {
			Parameter param = field.getAnnotation(Parameter.class);
			if (param != null) {
				try {
					field.setAccessible(true);
					Object value = field.get(entity);

					if (value!=null || param.required()) {
						if (value == null) {
							value = field.getType().newInstance();
							field.set(entity, value);
						}
						String property = param.property();
						map.put(property, value);
						configure(value);
					}
				} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
					throw new ConfigurationException(e);
				}
			}
		}
		
	}


	private void injectSimpleDefaults(List<Field> fieldList, Object entity) throws ConfigurationException {
		
		for (Field field : fieldList) {
			injectField(entity, field);
		}
		
	}


	private Object injectField(Object entity, Field field) throws ConfigurationException {
		Class<?> type = field.getType();
		Object value = null;
		if (isSimpleType(type)) {
			Parameter param = field.getAnnotation(Parameter.class);
			if (param != null) {
				try {
					value = value(entity, field, param);
					if (value != null) {
						field.setAccessible(true);
						if (type == String.class) {
							field.set(entity, value.toString());
						} else if (type == File.class) {
							if (value instanceof String) {
								field.set(entity,  new File(value.toString()));
							} else if (value instanceof File) {
								field.set(entity,  value);
							}
						}
					}
				} catch (Throwable e) {
					throw new ConfigurationException(e);
				}
			}
		}
		return value;
		
	}
	
	

	private boolean isSimpleType(Class<?> type) {
		return type.isPrimitive() || type==String.class || type==File.class;
	}

	private Object value(Object entity, Field field, Parameter param) throws ConfigurationException {
		
		String propertyName = param.property();
		Object propertyValue = map.get(propertyName);
		if (propertyValue != null) {
			return propertyValue;
		}
		
		propertyValue = globalProperties.getProperty(propertyName);
		if (propertyValue != null) {
			map.put(propertyName, propertyValue);
			return propertyValue;
		}
		
		
		try {
			field.setAccessible(true);
			Object value = field.get(entity);
			if (value != null) {
				propertyValue = value.toString();
				map.put(propertyName, propertyValue);
				return null;
			}
			
			
			String defaultValue = param.defaultValue();
			if (Parameter.UNDEFINED.equals(defaultValue)) {
				return null;
			}
			List<Element> elements = parseValue(defaultValue);
			StringBuilder buffer = new StringBuilder();
			for (Element e : elements) {
				switch (e.type) {
				case LITERAL :
					buffer.append(e.text);
					break;
					
				case VARIABLE :
					value = value(e.text);
					if (value == null) {
						buffer.append("${");
						buffer.append(e.text);
						buffer.append('}');
					} else {
						buffer.append(value.toString());
					}
					break;
				}
			}
			propertyValue = buffer.toString();
			buffer.setLength(0);
			map.put(propertyName, propertyValue);
			return propertyValue;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ConfigurationException(e);
		}
	}

	
	private Object value(String propertyName) throws ConfigurationException {
		Object result = globalProperties.get(propertyName);
		if (result == null) {
			result = map.get(propertyName);
			if (result == null) {
				result = reflectedValue(propertyName);
			}
		}
		return result;
	}


	private Object reflectedValue(String propertyName) throws ConfigurationException {
		
		int mark=0;
		for (mark=propertyName.indexOf('.'); mark>0; mark=propertyName.indexOf('.', mark+1)) {
			String objectName = propertyName.substring(0, mark);
			
			Object object = globalProperties.get(objectName);
			if (object == null) {
				object = map.get(objectName);
			}
			
			if (object != null) {
				String path = propertyName.substring(mark+1);
				String[] fieldList = path.split("[.]");
				Object parent = object;
				for (String fieldName : fieldList) {
					String getterName = getterName(fieldName);
					Class<?> type = object.getClass();
					try {
						Method getter = type.getMethod(getterName);
						object = getter.invoke(object);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
							IllegalArgumentException | InvocationTargetException e) {
						// Do nothing
					}
					if (object == null) {
						// Getter did not exist or returned null;
						// Check for field with Parameter annotation
						
						Field field = field(type, fieldName);
						if (field != null) {
							object = injectField(parent, field);
						}
						
						if (object == null) {
							return null;
						}
						parent = object;
						
					}
					
					if (!isSimpleType(object.getClass())) {
						map.put(objectName, object);
						configure(object);
					}
				}
				if (object != null) {
					return object;
				}
			}
		}
		
		
		return null;
	}

	private Field field(Class<?> type, String fieldName) {
		Field[] array = type.getDeclaredFields();
		for (Field field : array) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		
		Class<?> superType = type.getSuperclass();
		if (superType != null && superType != Object.class) {
			return field(superType, fieldName);
		}
		
		return null;
	}

	private String getterName(String fieldName) {
		StringBuilder builder = new StringBuilder();
		builder.append("get");
		builder.append(Character.toUpperCase(fieldName.charAt(0)));
		for (int i=1; i<fieldName.length(); i++) {
			builder.append(fieldName.charAt(i));
		}
		return builder.toString();
	}

	private void addAllFields(List<Field> fieldList, Class<?> type) {
		fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
		
		if (type.getSuperclass() != null) {
			addAllFields(fieldList, type.getSuperclass());
		}
	}
	
	private List<Element> parseValue(String fullText) {
		StringBuilder buffer = new StringBuilder();
		buffer.setLength(0);
		
		List<Element> list = new ArrayList<>();
		
		Element.Type type = Element.Type.LITERAL;
		for (int i=0; i<fullText.length(); i++) {
			char c = fullText.charAt(i);
			switch (type) {
			case LITERAL:
				if (c == '$') {
					char cc = fullText.charAt(++i);
					if (cc == '{') {
						addElement(buffer, list, type);
						type = Element.Type.VARIABLE;
					} else {
						buffer.append(c);
						buffer.append(cc);
					}
				} else {
					buffer.append(c);
				}
				break;
				
			case VARIABLE :
				if (c == '}') {
					addElement(buffer, list, type);
					type = Element.Type.LITERAL;
				} else {
					buffer.append(c);
				}
				break;
			}
		}
		if (buffer.length()>0) {
			addElement(buffer, list, type);
		}
		
		return list;
	}
	
	private void addElement(StringBuilder buffer, List<Element> list, Type type) {
	
		if (buffer.length()>0) {
			list.add(new Element(type, buffer.toString()));
			buffer.setLength(0);
		}
		
	}

	static class Element {
		static enum Type {
			VARIABLE,
			LITERAL
		}
		Type type;
		String text;
		
		public Element(Type type, String text) {
			this.type = type;
			this.text = text;
		}
		
		
		
	}

}
