package io.konig.yaml;

import java.io.File;
import java.io.PrintWriter;
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

public class YamlWriter {
	private int indent;
	private int indentSpaces = 3;
	private static final String RESERVED = ":{}[],&*#?|-<>=!%@`";

	private Map<Object, Integer> objectMap = new HashMap<Object, Integer>();
	private int count = 0;
	private boolean showClassTag = true;

	private PrintWriter out;
	
	public YamlWriter(Writer writer) {
		out = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);
	}
	
	public boolean isShowClassTag() {
		return showClassTag;
	}



	public void setShowClassTag(boolean showClassTag) {
		this.showClassTag = showClassTag;
	}



	public void write(Object obj) {
		printObject(obj);
		out.flush();
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
		if (objectId == null) {
			objectMap.put(object, objectId = new Integer(++count));
			if (showClassTag) {
				out.print('!');
				out.print(object.getClass().getName());
				out.print(' ');
			}
			out.print('&');
			out.print('x');
			println(objectId.toString());
			printProperties(object);
		} else {
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
	public void field(String fieldName, Object value) {
		if (value == null 
				|| (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())
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
	
	private void printValue(Object value) {
		if (value == null) {
			println("null");
		}
		if (value instanceof URI) {
			out.print('<');
			out.print(((URI) value).stringValue());
			println('>');
		} else if (value instanceof BNode) {
			out.print("_:");
			println(((BNode) value).getID());
		} else if (value instanceof Literal) {
			printLiteral((Literal) value);
		} else if (isSimpleValue(value)) {
			yamlPrintln(value.toString());
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
}
