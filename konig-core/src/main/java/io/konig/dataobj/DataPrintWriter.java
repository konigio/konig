package io.konig.dataobj;

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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.turtle.TurtleUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class DataPrintWriter extends PrintWriter {
	private int indent;
	private int indentSpaces = 3;

	private Map<Object, Integer> objectMap = new HashMap<Object, Integer>();
	private int count = 0;

	public DataPrintWriter(Writer out) {
		super(out);
	}

	public DataPrintWriter(OutputStream out) {
		super(out);
	}

	public DataPrintWriter(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public DataPrintWriter(File file) throws FileNotFoundException {
		super(file);
	}

	public DataPrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public DataPrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public DataPrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
	}

	public DataPrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
	}

	public void println() {
		print('\n');
	}

	public void push() {
		indent++;
	}

	public void pop() {
		indent--;
	}

	public void indent() {
		for (int i = 0; i < indent * indentSpaces; i++) {
			print(' ');
		}
	}

	public void field(String fieldName, Object value) {
		if (value == null 
				|| (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())
				|| (value.getClass().isArray() && Array.getLength(value) == 0)) {
			return;
		}
		indent();
		print(fieldName);
		print(' ');
		printValue(value);
	}

	private void printValue(Object value) {
		if (value == null) {
			println("null");
		}
		if (value instanceof URI) {
			print('<');
			print(((URI) value).stringValue());
			println('>');
		} else if (value instanceof BNode) {
			print("_:");
			println(((BNode) value).getID());
		} else if (value instanceof Literal) {
			printLiteral((Literal) value);
		} else if (isSimpleValue(value)) {
			println(value.toString());
		} else if (value instanceof Iterable) {
			printIterable((Iterable<?>) value);
		} else {
			printObject(value);
		}

	}

	private void printIterable(Iterable<?> sequence) {
		println();
		push();
		for (Object obj : sequence) {
			indent();
			print("- ");
			printValue(obj);
		}
		pop();

	}

	public void printObject(Object value) {

		Class<?> type = value.getClass();
		Integer ordinal = objectMap.get(value);
		if (ordinal == null) {
			count++;
			objectMap.put(value, count);
			print('&');
			print(type.getSimpleName());
			print(':');
			println(count);
			printProperties(value);
		} else {
			print('*');
			print(type.getSimpleName());
			print(':');
			println(ordinal);
		}
	}

	public void printObjectId(Object value) {
		Class<?> type = value.getClass();
		Integer ordinal = objectMap.get(value);
		if (ordinal == null) {
			count++;
			objectMap.put(value, count);
			print('&');
			print(type.getSimpleName());
			print(':');
			println(count);
		} else {
			print('*');
			print(type.getSimpleName());
			print(':');
			println(ordinal);
		}
	}

	private void printProperties(Object obj) {

		Class<?> type = obj.getClass();
		Method[] methodList = type.getMethods();
		List<Method> list = new ArrayList<Method>();
		JsonIgnoreProperties note = type.getAnnotation(JsonIgnoreProperties.class);
		DataObjectHiddenProperties hiddenNote = type.getAnnotation(DataObjectHiddenProperties.class);

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

		push();
		for (Method m : list) {
			String name = getterFieldName(m);

			try {
				Object value = m.invoke(obj);
				field(name, value);

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		pop();

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

	private void printLiteral(Literal literal) {

		String label = literal.getLabel();
		URI datatype = literal.getDatatype();

		if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
				|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype)) {
			try {
				print(XMLDatatypeUtil.normalize(label, datatype));
				return;
			} catch (IllegalArgumentException e) {
				// not a valid numeric typed literal. Ignore error and write as
				// quoted string instead.
			}
		}

		if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
			// Write label as long string
			write("\"\"\"");
			write(TurtleUtil.encodeLongString(label));
			write("\"\"\"");
		} else {
			// Write label as normal string
			write("\"");
			write(TurtleUtil.encodeString(label));
			write("\"");
		}

		if (Literals.isLanguageLiteral(literal)) {
			// Append the literal's language
			write("@");
			write(literal.getLanguage());
		} else if (!XMLSchema.STRING.equals(datatype)) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			write("^^");
			print('<');
			print(datatype.stringValue());
			println('>');
		}

	}

	private boolean isSimpleValue(Object value) {
		Class<?> type = value.getClass();
		return isSimpleType(type);

	}

	private boolean isSimpleType(Class<?> type) {
		return type.isEnum() || type.isPrimitive() || type == String.class || type == Boolean.class
				|| type == Byte.class || type == Character.class || type == Short.class || type == Integer.class
				|| type == Long.class || type == Double.class || type == Float.class
				|| Value.class.isAssignableFrom(type);
	}

}
