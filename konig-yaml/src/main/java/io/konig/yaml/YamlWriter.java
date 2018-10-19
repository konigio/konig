package io.konig.yaml;

/*
 * #%L
 * Konig YAML
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.turtle.TurtleUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class YamlWriter extends YamlWriterConfig  implements AutoCloseable {
	private int indent;
	private static final String RESERVED = ":{}[],&*#?|-<>=!%@`";
	private static final Integer MINUS_ONE = new Integer(-1);

	private Map<Object, Integer> objectMap = new HashMap<Object, Integer>();
	private int count = 0;

	private PrintWriter out;
	
	
	
	public YamlWriter(Writer writer) {
		out = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);
	}
	

	public void write(Object obj) {
		if (anchorFeature == AnchorFeature.SOME) {
			computeObjectCount(obj);
		}
		printObject(obj);
		out.flush();
	}
	
	




	private void countProperties(Object obj) {
		Class<?> type = obj.getClass();
		Method[] methodList = type.getMethods();
		List<Method> list = new ArrayList<Method>();
		JsonIgnoreProperties note = type.getAnnotation(JsonIgnoreProperties.class);
		HiddenProperties hiddenNote = type.getAnnotation(HiddenProperties.class);

		String[] ignore = note == null ? null : note.value();
		String[] hidden = hiddenNote == null ? null : hiddenNote.value();

		for (Method m : methodList) {
			String name = m.getName();
			if (name.length() > 3 && name.startsWith("get") && Modifier.isPublic(m.getModifiers())
					&& m.getParameterTypes().length == 0 && !name.equals("getClass")
					&& (ignore == null || wanted(m, ignore)) && (hidden == null || wanted(m, hidden))) {
				list.add(m);
			}
		}

		
		for (Method m : list) {
			if (Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			try {
				Object value = m.invoke(obj);
				computeObjectCount(value);

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		
	}


	private void computeObjectCount(Object object) {
		if (
			object == null ||
			(object instanceof Collection<?> && ((Collection<?>) object).isEmpty()) ||
			(object.getClass().isArray() && Array.getLength(object) == 0) ||
			isSimpleValue(object)
		) {
			return;
		}
		if (object instanceof Iterable<?>) {
			computeObjectCountsInCollection((Iterable<?>)object);
		} if (object.getClass().isArray()) {
			computeObjectCountsInCollection(Arrays.asList(object));
		} else {

			Integer count = objectMap.get(object);
			if (count == null) {
				objectMap.put(object, MINUS_ONE);
				countProperties(object);
			} else {
				count = count - 1;
				objectMap.put(object, count);
			}
		}
		
	}


	private void computeObjectCountsInCollection(Iterable<?> sequence) {
		for (Object value : sequence) {
			computeObjectCount(value);
		}
		
	}


	private void printObject(Object object) {
		if (object == null) {
			out.print("null");
			return;
		}
		if (object instanceof File) {
			File file = (File) object;
			printValue(file.getAbsolutePath());
			return;
		}
		Integer objectId = objectMap.get(object);
		boolean newAnchor = false;
		
		switch (anchorFeature) {
		case ALL :
			
			if (objectId == null) {
				newAnchor = true;
				objectId = new Integer(++count);
				objectMap.put(object, objectId);
			}
			break;
			
		case SOME:
			if (objectId == null) {
				throw new RuntimeException("Object count is missing");
			}
			if (objectId == -1) {
				objectId = null;
			} else if (objectId < -1) {
				newAnchor = true;
				objectId = new Integer(++count);
				objectMap.put(object, objectId);
			}
			break;
			
		case NONE:
//			if (objectId != null) {
//				
//				YamlWriterConfig config = new YamlWriterConfig().setIncludeClassTag(false);
//				String text = Yaml.toString(config, object);
//				
//				throw new RuntimeException("Repeated object not permitted with anchorFeature=NONE.\n" + text);
//			}
//			objectMap.put(object, MINUS_ONE);
			objectId = null;
			newAnchor = true;
			break;
		}
		
		if (newAnchor) {
			if (includeClassTag) {
				out.print('!');
				out.print(object.getClass().getName());
				out.print(' ');
			}
			if (objectId != null) {
				out.print('&');
				out.print('x');
				println(objectId.toString());
			} else {
				println();
			}
			printProperties(object);
		} else if (objectId != null) {
			out.print('*');
			out.print('x');
			println(objectId.toString());
		}
	}
	
	private void println(char c) {
		out.print(c);
		out.print('\n');
	}
	
	private void println() {
		out.print('\n');
	}
	private void println(String text) {
		out.print(text);
		out.print('\n');
	}
	
	private void printProperties(Object obj) {
		Class<?> type = obj.getClass();
		Method[] methodList = type.getMethods();
		List<Method> list = new ArrayList<Method>();
		JsonIgnoreProperties note = type.getAnnotation(JsonIgnoreProperties.class);
		HiddenProperties hiddenNote = type.getAnnotation(HiddenProperties.class);

		String[] ignore = note == null ? null : note.value();
		String[] hidden = hiddenNote == null ? null : hiddenNote.value();

		for (Method m : methodList) {
			String name = m.getName();
			if (name.length() > 3 && name.startsWith("get") && Modifier.isPublic(m.getModifiers())
					&& m.getParameterTypes().length == 0 && !name.equals("getClass")
					&& (ignore == null || wanted(m, ignore)) && (hidden == null || wanted(m, hidden))) {
				list.add(m);
			}
		}

		Collections.sort(list, new Comparator<Method>() {
			public int compare(Method a, Method b) {

				Class<?> aType = a.getReturnType();
				Class<?> bType = b.getReturnType();

				boolean aSimple = isSimpleType(aType);
				boolean bSimple = isSimpleType(bType);

				if (aSimple && !bSimple) {
					return -1;
				}
				if (bSimple && !aSimple) {
					return 1;
				}
				return a.getName().compareTo(b.getName());
			}
		});
		
		boolean doPush = true;
		if (anchorFeature==AnchorFeature.NONE) {
			if (count==0) {
				count=1;
				doPush = false;
			}
		}

		if (doPush) {
			push();
		}
		for (Method m : list) {
			if (Modifier.isStatic(m.getModifiers())) {
				continue;
			}
			String name = getterFieldName(m);

			try {
				Object value = m.invoke(obj);
				field(name, value);

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		if (doPush) {
			pop();
		}
		
	}
	public void field(String fieldName, Object value) {
		if (value == null 
				|| (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())
				|| (value instanceof Map<?,?> && ((Map<?,?>)value).isEmpty())
				|| (value.getClass().isArray() && Array.getLength(value) == 0)) {
			return;
		}
		indent();
		out.print(fieldName);
		out.print(": ");
		printValue(value);
	}
	
	private boolean wanted(Method m, String[] ignore) {
		String name = getterFieldName(m);
		for (String n : ignore) {
			if (name.equals(n)) {
				return false;
			}
		}
		return true;
	}


	private String getterFieldName(Method m) {
		String name = m.getName();
		char c = Character.toLowerCase(name.charAt(3));

		StringBuilder builder = new StringBuilder(name.length() - 3);
		builder.append(c);
		for (int i = 4; i < name.length(); i++) {
			builder.append(name.charAt(i));
		}

		return builder.toString();
	}

	private boolean isSimpleType(Class<?> type) {
		return type.isEnum() || type.isPrimitive() || type == String.class || type == Boolean.class
				|| type == Byte.class || type == Character.class || type == Short.class || type == Integer.class
				|| type == Long.class || type == Double.class || type == Float.class 				
				|| Value.class.isAssignableFrom(type);
	}

	private void push() {
		indent++;
	}
	
	private void pop() {
		indent--;
	}
	
	private void indent() {
		for (int i=0; i<indent*indentSpaces; i++) {
			out.print(' ');
		}
	}
	
	@SuppressWarnings("unchecked")
	private void printValue(Object value) {
		if (value == null) {
			println("null");
		}
		if (value instanceof URI) {
			out.println(((URI) value).stringValue());
		} else if (value instanceof BNode) {
			out.print("_:");
			println(((BNode) value).getID());
		} else if (value instanceof Literal) {
			printLiteral((Literal) value);
		} else if (isSimpleValue(value)) {
			yamlPrintln(value.toString());
		} else if (value instanceof Iterable) {
			printIterable((Iterable<?>) value);
		} else if (value instanceof Map) {
			printMap((Map<Object,Object>) value);
		} else {
			printObject(value);
		}

	}


	private void printMap(Map<Object,Object> map) {
		if (map.isEmpty()) {
			return;
		}
		
		println();
		push();
		List<Entry<Object,Object>> list = new ArrayList<>(map.entrySet());
		
		Collections.sort(list, new Comparator<Entry<Object,Object>>(){

			@Override
			public int compare(Entry<Object, Object> a, Entry<Object, Object> b) {
				String x = a.getKey().toString();
				String y = b.getKey().toString();
				return x.compareTo(y);
			}});
		
		for (Entry<Object,Object>  e : list) {
			String key = e.getKey().toString();
			Object value = e.getValue();
			indent();
			if (key.indexOf(':') >= 0) {
				out.print('"');
				out.print(key);
				out.print('"');
			} else {
				out.print(key);
			}
			out.print(": ");
			printValue(value);
		}
		
		pop();
		
	}


	private void printIterable(Iterable<?> sequence) {
		println();
		push();
		for (Object obj : sequence) {
			indent();
			out.print("- ");
			printValue(obj);
		}
		pop();

	}

	public void yamlPrintln(String value) {
		yamlPrint(value);
		println();
	}
	
	public void yamlPrint(String value) {
		if (value == null) {
			out.print("null");
		} else {
			
			if (value.length()==0) {
				out.print("\"\"");
			} else {
				char first = value.charAt(0);
				char last = value.charAt(value.length()-1);
				if (Character.isWhitespace(first) || Character.isWhitespace(last) || RESERVED.indexOf(first)>=0) {
					printQuotedString(value);
				} else {
					out.print(value);
				}
			}
		}
	}
	
	private void printQuotedString(String value) {
		
		char quote = '\'';
		
		out.print(quote);
		if (value.indexOf(quote)>=0) {
			for (int i=0; i<value.length(); i++) {
				char c = value.charAt(i);
				if (c == quote) {
					out.print(quote);
				}
				out.print(c);
			}
		} else {
			out.print(value);
		}
		
		out.print(quote);
		
	}

	private boolean isSimpleValue(Object value) {
		Class<?> type = value.getClass();
		return isSimpleType(type);

	}

	private void printLiteral(Literal literal) {

		String label = literal.getLabel();
		URI datatype = literal.getDatatype();

		if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
				|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype)) {
			try {
				out.print(XMLDatatypeUtil.normalize(label, datatype));
				return;
			} catch (IllegalArgumentException e) {
				// not a valid numeric typed literal. Ignore error and write as
				// quoted string instead.
			}
		}

		if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
			// Write label as long string
			out.write("\"\"\"");
			out.write(TurtleUtil.encodeLongString(label));
			out.write("\"\"\"");
		} else {
			// Write label as normal string
			out.write("\"");
			out.write(TurtleUtil.encodeString(label));
			out.write("\"");
		}

		if (Literals.isLanguageLiteral(literal)) {
			// Append the literal's language
			out.write("@");
			out.write(literal.getLanguage());
		} else if (!XMLSchema.STRING.equals(datatype)) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			out.write("^^");
			out.print('<');
			out.print(datatype.stringValue());
			println('>');
		}

	}

	@Override
	public void close() throws IOException {
		out.close();
	}
}
