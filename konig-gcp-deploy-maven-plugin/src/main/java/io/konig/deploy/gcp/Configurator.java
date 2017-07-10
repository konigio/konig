package io.konig.deploy.gcp;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.konig.deploy.gcp.Configurator.Element.Type;


public class Configurator {
	private Properties globalProperties;
	private Map<String, String> map = new HashMap<>();
	private StringBuilder buffer = new StringBuilder();
	
	public Configurator(Properties globalProperties) {
		this.globalProperties = globalProperties;
	}


	public void configure(Object entity) throws DeploymentException {
		Class<?> type = entity.getClass();
		List<Field> fieldList = new ArrayList<>();
		addAllFields(fieldList, type);
		
		injectSimpleDefaults(fieldList, entity);
		injectObjects(fieldList, entity);
		
	}


	private void injectObjects(List<Field> fieldList, Object entity) throws DeploymentException {
		
		for (Field field : fieldList) {
			Parameter param = field.getAnnotation(Parameter.class);
			if (param != null) {
				try {
					field.setAccessible(true);
					Object value = field.get(entity);
					if (value != null) {
						configure(value);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new DeploymentException(e);
				}
			}
		}
		
	}


	private void injectSimpleDefaults(List<Field> fieldList, Object entity) throws DeploymentException {
		
		for (Field field : fieldList) {
			Class<?> type = field.getType();
			if (type==String.class || type==File.class) {
				Parameter param = field.getAnnotation(Parameter.class);
				if (param != null) {
					try {
						String value = value(entity, field, param);
						if (value != null) {
							field.setAccessible(true);
							if (type == String.class) {
								field.set(entity, value);
							} else if (type == File.class) {
								field.set(entity,  new File(value));
							}
						}
					} catch (Throwable e) {
						throw new DeploymentException(e);
					}
				}
			}
		}
		
	}


	private String value(Object entity, Field field, Parameter param) throws DeploymentException {
		
		String propertyName = param.property();
		String propertyValue = map.get(propertyName);
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
			buffer.setLength(0);
			for (Element e : elements) {
				switch (e.type) {
				case LITERAL :
					buffer.append(e.text);
					break;
					
				case VARIABLE :
					value = map.get(e.text);
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
			throw new DeploymentException(e);
		}
		
	}


	private void addAllFields(List<Field> fieldList, Class<?> type) {
		fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
		
		if (type.getSuperclass() != null) {
			addAllFields(fieldList, type.getSuperclass());
		}
	}
	
	private List<Element> parseValue(String fullText) {
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
						addElement(list, type);
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
					addElement(list, type);
					type = Element.Type.LITERAL;
				} else {
					buffer.append(c);
				}
				break;
			}
		}
		if (buffer.length()>0) {
			addElement(list, type);
		}
		
		return list;
	}
	
	private void addElement(List<Element> list, Type type) {
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
