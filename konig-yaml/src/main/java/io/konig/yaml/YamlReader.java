package io.konig.yaml;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlReader {
	

	private PushbackReader reader;
	private Map<String,Object> objectMap = new HashMap<>();
	private StringBuilder buffer = new StringBuilder();
	private Map<Class<?>, ClassInfo> classInfo = new HashMap<>();
	private Method addMethod;
	
	private int nextIndentWidth;
	
	public YamlReader(Reader reader) {
		this.reader = new PushbackReader(reader, 2);
	}

	private void fail(String msg) throws YamlParseException {
		throw new YamlParseException(msg);
		
	}

	private Object objectRef() throws IOException, YamlParseException {
		assertNext('*');
		String objectId = readWord();
		Object pojo = objectMap.get(objectId);
		if (pojo == null) {
			fail("Object not found: *" + objectId);
		}
		
		return pojo;
	}

	private Object typedObject(YamlParser parent, Object pojo, Method setter) throws YamlParseException, IOException {
		
		ObjectYamlParser next = new ObjectYamlParser(parent);
		if (next.indentWidth == Integer.MAX_VALUE) {
			return next.pojo;
		}
		try {
			setter.invoke(pojo, next.pojo);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new YamlParseException("Failed to invoke setter: " + setter.getName(), e);
		}
		
		return next;
	}

	private ClassInfo classInfo(Class<?> type) {
		ClassInfo info = classInfo.get(type);
		if (info == null) {
			info = createClassInfo(type);
			classInfo.put(type, info);
		}
		return info;
	}

	private int indentWidth() throws IOException {
		int width = 0;
		int c = read();
		while (c == ' ') {
			width++;
			c = read();
		}
		unread(c);
		nextIndentWidth = c==-1 ? -1 : width;
		return nextIndentWidth;
	}

	private int valueStart() throws IOException {
		int c = read();
		while (c!=-1 && (c==' ' || c=='\t')) {
			c = read();
		}
		unread(c);
		return c;
	}

	
	


	@SuppressWarnings("unchecked")
	private Object scalarValue(Class<?> javaType) throws YamlParseException, IOException {
		String stringValue = stringValue();
		Object result = null;
		if (javaType == String.class) {
			result = stringValue;
		} else if (javaType == int.class || javaType == Integer.class) {
			result = new Integer(stringValue);
			
		} else if (javaType.isEnum()) {
			
			result = Enum.valueOf((Class<Enum>)javaType, stringValue);
		} else {
			
			fail("Unsupported scalar type: " +javaType.getName());
		}
		return result;
	}

	private String stringValue() throws IOException, YamlParseException {
		int c = read();
		unread(c);
		if (c == '"') {
			return doubleQuotedString();
		}
		
		if (c == '\'') {
			return singleQuotedString();
		}
		
		return unquotedString();
		
	}

	private String unquotedString() throws IOException {
		int c = read();
		StringBuilder buffer = buffer();
		while (c != -1 && c != '\n') {
			buffer.appendCodePoint(c);
			c = read();
		}
		unread(c);
		return buffer.toString();
	}

	private String doubleQuotedString() throws YamlParseException, IOException {
		assertNext('"');
		StringBuilder buffer = buffer();
		int c = read();
		while (c != '"') {
			// TODO: handle escaped quote
			buffer.appendCodePoint(c);
			c = read();
		}
		
		return buffer.toString();
	}

	private String singleQuotedString() throws YamlParseException, IOException {
		assertNext('\'');
		StringBuilder buffer = buffer();
		int c = read();
		while (c != '\'') {
			// TODO: handle escaped quote
			buffer.appendCodePoint(c);
			c = read();
		}
		
		return buffer.toString();
	}

	private void assertLineEnd() throws IOException, YamlParseException {
		int c = read();
		while (c != '\n' && c!=-1) {
			if (!Character.isWhitespace(c)) {
				throw new YamlParseException("Expected end-of-line but found '" + ((char)c) + "'");
			}
			c = read();
		}
	}

	private void unread(int c) throws IOException {
		if (c!=-1 && reader!=null) {
			reader.unread(c);
		}
	}

	private int read() throws IOException {
		if (reader == null) {
			return -1;
		}
		int c = reader.read();
		if (c == -1) {
			reader = null;
		} 
		return c;
	}

	private void skipSpace() throws IOException {
		int c = read();
		while (Character.isWhitespace(c)) {
			c = read();
		}
		unread(c);
	}

	private String readWord() throws IOException {
		StringBuilder buffer = buffer();
		int c = read();
		while (c>0 && !Character.isWhitespace(c)) {
			buffer.appendCodePoint(c);
			c = read();
		}
		unread(c);
	
		return buffer.toString();
	}

	private StringBuilder buffer() {
		buffer.setLength(0);
		return buffer;
	}

	private void assertNext(int expected) throws YamlParseException, IOException {
		int actual = next();
		if (expected != actual) {
			String msg = MessageFormat.format("Expected '{0}' but found '{1}'", expected, actual);
			throw new YamlParseException(msg);
		}
	}
	
	private int next() throws IOException {
		int c = read();
		while (Character.isWhitespace(c)) {
			c = read();
		}
		
		return c;
	}


	

	private static class ClassInfo {
		private Map<String, Method> setterMethod = new HashMap<>();
		private Map<String, Method> adderMethod = new HashMap<>();
		

	}
	
	private ClassInfo createClassInfo(Class<?> type) {
		ClassInfo info = new ClassInfo();
		Method[] methodList = type.getMethods();
		for (Method m : methodList) {
			String name = m.getName();
			YamlProperty note = m.getAnnotation(YamlProperty.class);
			if (note != null) {
				String fieldName = note.value();
				if (fieldName.startsWith("add")) {
					info.adderMethod.put(fieldName, m);
				} else {
					info.setterMethod.put(fieldName, m);
				}
			} else if (m.getParameterTypes().length==1 && name.length()>3) {
				if (name.startsWith("set")) {
					String fieldName = fieldName(name);
					info.setterMethod.put(fieldName, m);
				} else if (name.startsWith("add")) {
					String fieldName = fieldName(name);
					info.adderMethod.put(fieldName, m);
				}
			}
		}
		return info;
	}

	private String fieldName(String methodName) {
		StringBuilder buffer = buffer();
		char c = methodName.charAt(3);
		buffer.append(Character.toLowerCase(c));
		for (int i=4; i<methodName.length(); i++) {
			buffer.append(methodName.charAt(i));
		}
		return buffer.toString();
	}

	
	public Object readObject() throws YamlParseException, IOException {
		ObjectYamlParser objectParser = new ObjectYamlParser(null);
		YamlParser parser = objectParser;
		while (parser != null) {
			parser = parser.nextParser();
		}
		
		return objectParser.pojo;

	}
	private abstract class YamlParser {
		protected YamlParser parent;
		protected int indentWidth;
		
		public YamlParser(YamlParser parent) {
			this.parent = parent;
		}

		abstract YamlParser nextParser() throws YamlParseException, IOException;
		
		protected YamlParser findNext() throws IOException {
			if (nextIndentWidth < 0) {
				nextIndentWidth = indentWidth();
			}
			if (nextIndentWidth < 0) {
				return null;
			}
			
			YamlParser parser = this;
			while (parser != null && parser.indentWidth!=nextIndentWidth) {
				parser = parser.parent;
			}
			return parser;
		}
	}
	
	private class CollectionYamlParser extends YamlParser {
		private Object pojo;
		private Method adder;

		public CollectionYamlParser(YamlParser parent, Object pojo, Method adder) throws IOException, YamlParseException {
			super(parent);
			this.pojo = pojo;
			this.adder = adder;
			int c = read();
			if (c != '\n') {
				throw new YamlParseException("Expected new line character");
			}
			indentWidth = indentWidth();
		}

		

		@Override
		YamlParser nextParser() throws YamlParseException, IOException {

			nextIndentWidth=-1;
			assertNext('-');
			Object value = readValue(this, pojo, adder);
			if (value instanceof CollectionYamlParser) {
				// TODO: support collections of collections
				throw new YamlParseException("Collections of collections not supported");
			}
			if (value instanceof YamlParser) {
				return (YamlParser) value;
			}
			
			try {
				adder.invoke(pojo, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new YamlParseException("Failed to invoke adder: " + adder.getName());
			}
			
			
			return findNext();
		}
		
	}
	
	private class ObjectYamlParser extends YamlParser {

		private Object pojo = null;
		private ClassInfo classInfo;
		
		public ObjectYamlParser(YamlParser parent) throws YamlParseException, IOException {
			super(parent);
			assertNext('!');
			String javaType = readWord();
			
			try {
				Class<?> type = Class.forName(javaType);
				try {
					classInfo = classInfo(type);
					pojo = type.newInstance();
					assertNext('&');
					String objectId = readWord();
					objectMap.put(objectId, pojo);
					assertLineEnd();
					int nextWidth = indentWidth();
					if (nextWidth > indentWidth) {
						this.indentWidth = nextWidth;
					} else {
						this.indentWidth = Integer.MAX_VALUE;
					}
					
					
					
				} catch (InstantiationException | IllegalAccessException e) {
					fail("Failed to instantiate class " + javaType);
				}
				
			} catch (ClassNotFoundException e) {
				fail("Class not found: " + javaType);
			}
			
		}


		private String fieldName() throws IOException {
			StringBuilder buffer = buffer();
			int c = read();
			while (c!=':' && !Character.isWhitespace(c)) {
				buffer.appendCodePoint(c);
				c = read();
			}
			unread(c);
			return buffer.toString();
		}
		
		@Override
		YamlParser nextParser() throws YamlParseException, IOException {
			nextIndentWidth = -1;
			
			String fieldName = fieldName();
			skipSpace();
			assertNext(':');
			
			Method method = adderOrSetter(fieldName);
			Object value = readValue(this, pojo, method);
			if (value instanceof YamlParser) {
				YamlParser next = (YamlParser) value;
				next.parent = this;
				
				return next;
			} else {
				try {
					method.invoke(pojo, value);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new YamlParseException("Failed to set field: " + fieldName, e);
				}
			}
			
			
			return findNext();
		}




		private Method adderOrSetter(String fieldName) throws YamlParseException {
			Method m = classInfo.adderMethod.get(fieldName);
			if (m == null) {
				m = classInfo.setterMethod.get(fieldName);
			}
			if (m == null) {
				fail("Class " + pojo.getClass().getSimpleName() + " has no setter or adder for field: " + fieldName);
			}
			return m;
		}

		
	}
	
//	private ParseContext readProperties(ParseContext context) throws IOException, YamlParseException {
//		ParseContext nextContext = context;
//		while (nextContext == context) {
//			context.fieldName = fieldName();
//			Method method = context.setter = context.setterOrAdder();
//			if (method == null) {
//				throw new YamlParseException("Setter not found for field: " + context.fieldName);
//			}
//			Object pojo = context.pojo;
//			skipSpace();
//			assertNext(':');
//			
//			Object value = readValue(context);
//			if (value instanceof ParseContext) {
//				nextContext = (ParseContext) value;
//				continue;
//			}
//			
//			try {
//				method.invoke(pojo, value);
//				
//			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//				String msg = MessageFormat.format("Failed to set field '{0}' with value '{1}'", context.fieldName, value.toString());
//				fail(msg);
//				
//			}
//			assertLineEnd();
//			nextContext = nextContext(context);
//		}
//		
//		return nextContext;
//		
//	}

	private Object readValue(YamlParser parser, Object pojo, Method setter) throws IOException, YamlParseException {
		
		int c = valueStart();
		Object value = null;
		
		switch (c) {
		case '!' :
			value = typedObject(parser, pojo, setter);
			break;
			
		case '*' :
			value = objectRef();
			assertLineEnd();
			break;
			
		case '\n' :
			if (setter.getName().startsWith("add")) {
				value = new CollectionYamlParser(parser, pojo, setter);
			} else {
				List<?> list = new ArrayList<>();
				value = new CollectionYamlParser(parser, list, collectionAddMethod());
				try {
					setter.invoke(pojo, list);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new YamlParseException(e);
				}
			}
			break;
			
		default :
			Class<?> javaType = setter.getParameterTypes()[0];
			value = scalarValue(javaType);
			assertLineEnd();
			break;
			
		}
		
		return value;
	}

	private Method collectionAddMethod() {
		if (addMethod == null) {
			Method[] methodList = Collection.class.getMethods();
			for (Method m : methodList) {
				if (m.getName().equals("add")) {
					addMethod = m;
					break;
				}
			}
		}
		return addMethod;
	}
}
