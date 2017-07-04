package io.konig.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class YamlReader implements AutoCloseable {
	

	private PushbackReader reader;
	private Map<String,Object> objectMap = new HashMap<>();
	private StringBuilder buffer = new StringBuilder();
	private Map<Class<?>, ClassInfo> classInfo = new HashMap<>();
	private Method addMethod;
	
	private int nextIndentWidth;
	private boolean isObjectItem = false;
	private int lineNo = 1;
	
	public YamlReader(InputStream input) {
		this(new InputStreamReader(input));
	}
	public YamlReader(Reader reader) {
		this.reader = new PushbackReader(reader, 2);
	}
	
	public void addDeserializer(Class<?> type, YamlDeserializer deserializer)  {
		ClassInfo classInfo;
		try {
			classInfo = classInfo(type);
		} catch (YamlParseException e) {
			throw new RuntimeException(e);
		}
		classInfo.deserializer = deserializer;
	}

	private void fail(String msg, Throwable e) throws YamlParseException {
		StringBuilder builder = new StringBuilder();
		builder.append("Line ");
		builder.append(lineNo);
		builder.append(". ");
		builder.append(msg);
		throw new YamlParseException(builder.toString(), e);
	}
	private void fail(String msg) throws YamlParseException {
		StringBuilder builder = new StringBuilder();
		builder.append("Line ");
		builder.append(lineNo);
		builder.append(". ");
		builder.append(msg);
		throw new YamlParseException(builder.toString());
		
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

	private Object typedObject(YamlParser parent, ValueConsumer consumer) throws YamlParseException, IOException {
		
		ObjectYamlParser next = new ObjectYamlParser(parent, null);
		if (next.indentWidth == Integer.MAX_VALUE) {
			return next.pojo;
		}

		// Delay setting the typed object on the owner pojo until the typed object is fully defined.
		next.setConsumer(consumer);
		
		return next;
	}

	private ClassInfo classInfo(Class<?> type) throws YamlParseException {
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
		while (c==' ' || c=='\n') {
			if (c == '\n') {
				width = 0;
			} else {
				width++;
			}
			c = read();
		}
		unread(c);
		nextIndentWidth = c==-1 ? -1 : width;
		return nextIndentWidth;
	}

	private boolean isSpace(int c) {
		
		return c==' ' || c=='\t' || c=='\r';
	}

	private int valueStart() throws IOException {
		int c = read();
		while (c!=-1 && isSpace(c)) {
			c = read();
		}
		unread(c);
		return c;
	}

	
	


	private Object scalarValue(Class<?> javaType) throws YamlParseException, IOException {
		String stringValue = stringValue();
		return scalarValue(stringValue, javaType);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object scalarValue(String stringValue, Class<?> javaType) throws YamlParseException {
		Object result = null;
		if (javaType == String.class) {
			result = stringValue;
		} else if (javaType == int.class || javaType == Integer.class) {
			result = new Integer(stringValue);
			
		} else if (javaType.isEnum()) {
			
			result = Enum.valueOf((Class<Enum>)javaType, stringValue);
		} else {
			ClassInfo info = classInfo.get(javaType);
			if (info != null  && info.deserializer!=null) {
				return info.deserializer.fromString(stringValue);
			}
			
			Constructor<?>[] ctorList = javaType.getConstructors();
			for (Constructor<?> ctor : ctorList) {
				Class<?>[] paramList = ctor.getParameterTypes();
				if (paramList.length==1 && paramList[0] == String.class) {
					try {
						return ctor.newInstance(stringValue);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						fail(e);
					}
				}
			}
			
			fail("Unsupported scalar type: " +javaType.getName());
		}
		return result;
	}

	private void fail(Exception e) throws YamlParseException {
		if (e instanceof YamlParseException) {
			throw (YamlParseException) e;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Line ");
		builder.append(lineNo);
		builder.append(". ");
		throw new YamlParseException(builder.toString(), e);
		
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
		return buffer.toString().trim();
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
			if (c=='\n') {
				lineNo--;
			}
			reader.unread(c);
		}
	}

	private int read() throws IOException {
		if (reader == null) {
			return -1;
		}
		int c = reader.read();
		if (c=='\n') {
			lineNo++;
		}
		if (c == -1) {
			reader = null;
		} 
		return c;
	}
	
	private int skip(String tokens) throws IOException {
		int c = read();
		while (tokens.indexOf(c)>=0) {
			c = read();
		}
		unread(c);
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
			String msg = MessageFormat.format("Expected ''{0}'' but found ''{1}''", (char)expected, (char)actual);
			fail(msg);
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
		private YamlDeserializer deserializer = null;
		private Map<String, MapAdapter> mapAdapter = null;
		
		public void registerMap(String fieldName, MapAdapter adapter) {
			if (mapAdapter == null) {
				mapAdapter = new HashMap<>();
			}
			mapAdapter.put(fieldName, adapter);
		}
		
		public MapAdapter getMapAdapter(String fieldName) {
			return mapAdapter == null ? null : mapAdapter.get(fieldName);
		}

	}
	
	private ClassInfo createClassInfo(Class<?> type) throws YamlParseException {
		ClassInfo info = new ClassInfo();
		Method[] methodList = type.getMethods();
		for (Method m : methodList) {
			String name = m.getName();
			YamlProperty note = m.getAnnotation(YamlProperty.class);
			YamlMap mapNote = null;
			if (note != null && m.getParameterTypes().length==1) {
				String fieldName = note.value();
				if (fieldName.startsWith("add")) {
					info.adderMethod.put(fieldName, m);
				} else {
					info.setterMethod.put(fieldName, m);
				}
			} else if ((mapNote = m.getAnnotation(YamlMap.class)) != null) {
				try {
					String fieldName = mapNote.value();
					Class<?> valueType = m.getParameterTypes()[0];
					Constructor<?> valueConstructor = valueType.getConstructor(String.class);
					MapAdapter adapter = new MapAdapter(m, valueType, valueConstructor);
					info.registerMap(fieldName, adapter);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new YamlParseException(e);
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
		return  readObject(null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T readObject(Class<?> type) throws YamlParseException, IOException {
		indentWidth();
		ObjectYamlParser objectParser = null;
		try {
			objectParser = new ObjectYamlParser(type);
		} catch (Exception e) {
			fail(e);
		}
		YamlParser parser = objectParser;
		while (parser != null) {
			parser = parser.nextParser();
		}
		
		return (T) objectParser.pojo;

	}
	private abstract class YamlParser {
		protected YamlParser parent;
		protected int indentWidth;
		
		public YamlParser(YamlParser parent) {
			this.parent = parent;
		}

		abstract YamlParser nextParser() throws YamlParseException, IOException;
		
		public int getIndentWidth() {
			return indentWidth;
		}

		protected void exit() throws YamlParseException {
			
		}



		public void setIndentWidth(int indentWidth) {
			this.indentWidth = indentWidth;
		}

		protected String fieldName() throws IOException, YamlParseException {
			StringBuilder buffer = buffer();
			int c = read();
			if (c=='"' || c=='\'') {
				unread(c);
				return quotedString(c);
			}
			while (c!=':' && !Character.isWhitespace(c)) {
				buffer.appendCodePoint(c);
				c = read();
			}
			unread(c);
			String result = buffer.toString();
			return result;
		}
		
		private String quotedString(int c) throws YamlParseException, IOException {
			String fieldName = null;
			switch (c) {
			case '"' :
				fieldName = doubleQuotedString();
				break;
				
			case '\'' :
				fieldName = singleQuotedString();
				break;
				
			}
			
			return fieldName;
		}

		protected YamlParser findNext() throws YamlParseException, IOException {
			if (nextIndentWidth < 0) {
				nextIndentWidth = indentWidth();
			}
			YamlParser parser = this;
			if (nextIndentWidth < 0) {
				while (parser != null) {
					parser.exit();
					parser = parser.parent;
				}
				return null;
			}
			
			while (parser != null && parser.indentWidth!=nextIndentWidth) {
				parser.exit();
				parser = parser.parent;
			}
			return parser;
		}
	}
	
	private class CollectionYamlParser extends YamlParser {
		private ValueConsumer consumer;

		public CollectionYamlParser(YamlParser parent, ValueConsumer consumer) throws IOException, YamlParseException {
			super(parent);
			this.consumer = consumer;
			indentWidth = nextIndentWidth;
			
		}

		




		private Object parsePropertyOrScalar(int k) throws IOException, YamlParseException {
			String stringValue = null;

			int prev = -1;
			switch (k) {
			case '"' :
				stringValue = doubleQuotedString();
				prev=k;
				break;
				
			case '\'' :
				stringValue = singleQuotedString();
				prev=k;
				break;
			}

			
			StringBuilder buffer = buffer();
			k = read();
			while (k != -1) {
				if (k == '\n') {
					unread(k);
					break;
				}
				if (Character.isWhitespace(k)) {
					if (prev == ':') {
						// We have detected a property.
						// Therefore the list item is an object.
						
						unread(k);
						unread(':');

						if (stringValue == null) {
							buffer.setLength(buffer.length()-1);
							stringValue = buffer.toString();
						}
						ObjectYamlParser objectParser = new ObjectYamlParser(this, consumer);
						objectParser.setConsumer(consumer);
						objectParser.setFieldName(stringValue);
						isObjectItem = true;
						return objectParser;
					}
				}
				buffer.appendCodePoint(k);
				prev = k;
				k = read();
			}
			if (stringValue == null) {
				stringValue = buffer.toString().trim();
			}
			
			Object result = scalarValue(stringValue, consumer.getValueType());
			if (!(result instanceof YamlParser)) {
				try {
					consumer.consume(result);
				} catch (Exception e) {
					fail(e);
				}
			}
			return result;
			
		}






		@Override
		YamlParser nextParser() throws YamlParseException, IOException {

			nextIndentWidth=-1;
			assertNext('-');
			Object value = parseValue();
			if (value instanceof CollectionYamlParser) {
				// TODO: support collections of collections
				throw new YamlParseException("Collections of collections not supported");
			}
			if (value instanceof YamlParser) {
				return (YamlParser) value;
			}
			
			
			return findNext();
		}






		private Object parseValue() throws IOException, YamlParseException {

			int c = skip(" \t\r");
			switch (c) {
			case '!' :
			case '&' :
			case '*' :
			case '\n' :
				return readValue(this, consumer);
				
				
			default:
				return parsePropertyOrScalar(c);
			}
		}

		
	}
	
	static interface ObjectFactory {

		Class<?> getValueType();
		Object createObject() throws Exception;
	}
	
	static class BasicObjectFactory implements ObjectFactory {
		private Class<?> valueType;
		

		public BasicObjectFactory(Class<?> valueType) {
			this.valueType = valueType;
		}

		@Override
		public Class<?> getValueType() {
			return valueType;
		}

		@Override
		public Object createObject() throws Exception {
			return valueType.newInstance();
		}
		
	}
	
	static interface ValueConsumer extends ObjectFactory {
		void consume(Object value) throws Exception;
		boolean isAdder();
	}
	
	private static class PojoValueConsumer implements ValueConsumer {
		private Object pojo;
		private Method method;
		private boolean isAdder;
		private Class<?> valueType;
		
		public PojoValueConsumer(Object pojo, Method method) {
			this.pojo = pojo;
			this.method = method;
			isAdder = this.method.getName().startsWith("add");
			valueType = method.getParameterTypes()[0];
		}
		
		public PojoValueConsumer(Object pojo, Method method, Class<?> valueType) {

			this.pojo = pojo;
			this.method = method;
			isAdder = this.method.getName().startsWith("add");
			this.valueType = valueType;
		}
		
		@Override
		public void consume(Object value) throws Exception {
			method.invoke(pojo, value);
		}

		public String toString() {
			return "PojoValueConsumer(method=" + method.getName() + ", valueType=" + valueType.getName() + ")"; 
		}
		
		@Override
		public Class<?> getValueType() {
			return valueType;
		}
		
		@Override
		public boolean isAdder() {
			return isAdder;
		}
		@Override
		public Object createObject() throws Exception {
			return valueType.newInstance();
		}
		
	}
	
	private static class MapValueConsumer implements ValueConsumer {
		private Map<String,Object> map = new HashMap<>();
		private String fieldName;
		private Class<?> valueType;
		
		public MapValueConsumer(Map<String, Object> map, String fieldName, Class<?> valueType) {
			this.map = map;
			this.fieldName = fieldName;
			this.valueType = valueType;
		}
		
		@Override
		public Class<?> getValueType() {
			return valueType;
		}
		
		@Override
		public void consume(Object value) throws Exception {
			map.put(fieldName, value);
		}
		
		public MapValueConsumer forField(String fieldName) {
			return new MapValueConsumer(map, fieldName, valueType);
		}
		
		@Override
		public boolean isAdder() {
			return false;
		}

		@Override
		public Object createObject() throws Exception {
			Constructor<?> ctor = valueType.getConstructor(String.class);
			return ctor.newInstance(fieldName);
		}
		
		public String toString() {
			return 
				"MapValueConsumer(fieldName=" + fieldName + ", valueType=" + valueType.getName() + ")";
		}
		
	}
	private class MapYamlParser extends YamlParser {
		
		private MapValueConsumer consumer;

		public MapYamlParser(YamlParser parent, MapValueConsumer consumer) throws IOException, YamlParseException {
			super(parent);
			this.consumer = consumer;
			assertLineEnd();
			indentWidth = indentWidth();
		}

		@Override
		YamlParser nextParser() throws YamlParseException, IOException {
			String fieldName = fieldName();
			assertNext(':');
			
			MapValueConsumer fieldConsumer = consumer.forField(fieldName);
			Object value = readValue(this, fieldConsumer);
			if (value instanceof YamlParser) {
				return (YamlParser) value;
			}
			
			return findNext();
		}
		
	}
	
	private class MapAdapterYamlParser extends YamlParser {
		private MapAdapter adapter;
		private Object pojo;
		
		public MapAdapterYamlParser(YamlParser parent, Object pojo, MapAdapter adapter) throws IOException, YamlParseException {
			super(parent);
			this.pojo = pojo;
			this.adapter = adapter;
			assertLineEnd();
			indentWidth = indentWidth();
		}


		@Override
		YamlParser nextParser() throws YamlParseException, IOException {
			YamlParser next = null;
			String fieldName = fieldName();
			assertNext(':');
			try {
				Object value = adapter.getValueConstructor().newInstance(fieldName);
				// For now, we only support the case where object type and object reference is undefined.
				// TODO: support object type and object reference declarations
				assertLineEnd();
				
				int width = indentWidth();
				PojoValueConsumer consumer = new PojoValueConsumer(pojo, adapter.getAddMethod());
				
				next = new ObjectYamlParser(this, value, consumer, width);
			} catch (Exception e) {
				fail(e);
			}
			
			return next;
			
		}
		
	}
	
	
	private class ObjectYamlParser extends YamlParser {

		private Object pojo = null;
		private ClassInfo classInfo;
		private ValueConsumer consumer;
		private String fieldName;
		
		public ObjectYamlParser(Class<?> type) throws Exception {
			this(null, new BasicObjectFactory(type));
		}
		
		public ObjectYamlParser(YamlParser parent, Object pojo, ValueConsumer consumer, int indentWidth) throws YamlParseException {
			super(parent);
			this.pojo = pojo;
			this.consumer = consumer;
			this.indentWidth = indentWidth;
			classInfo = classInfo(pojo.getClass());
		}
		
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public ObjectYamlParser(YamlParser parent, ObjectFactory factory) throws YamlParseException, IOException {
			super(parent);
			int c = next();
			
			Class<?> type = null;
			if (c == '!') {

				String javaType = readWord();
				try {
					type = Class.forName(javaType);
					factory = null;
				} catch (ClassNotFoundException e) {
					fail("Class not found: " + javaType);
				}
				
			} else {
				type = factory.getValueType();
				unread(c);
			}
			
			
			try {
				classInfo = classInfo(type);
				pojo = factory==null ? type.newInstance() : factory.createObject();
				c = next();
				if (c == '&') {
					String objectId = readWord();
					objectMap.put(objectId, pojo);
					assertLineEnd();
					int nextWidth = indentWidth();
					if (nextWidth > indentWidth) {
						this.indentWidth = nextWidth;
					} else {
						this.indentWidth = Integer.MAX_VALUE;
					}
				} else {
					unread(c);
					this.indentWidth = nextIndentWidth;
				}
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Failed to instantiate class " + type.getName());
			}
				
			
		}



		public void setConsumer(ValueConsumer consumer) {
			this.consumer = consumer;
		}

		@Override
		YamlParser nextParser() throws YamlParseException, IOException {
			nextIndentWidth = -1;
			
			String fieldName = fieldName();
			skipSpace();
			int c = next();
			if (c == -1) {
				
				YamlParser parser = this;
				while (parser != null) {
					parser.exit();
					parser = parser.parent;
				}
				
				return null;
			}
			if (c != ':') {
				String msg = MessageFormat.format("Expected ':' but found ''{0}''", (char)c);
				fail(msg);
			}
			MapAdapterYamlParser mapParser = mapParser(fieldName);
			if (mapParser != null) {
				return mapParser;
			}
			
			ValueConsumer consumer = createValueConsumer(fieldName);
			if (consumer instanceof MapValueConsumer) {
				return new MapYamlParser(this, (MapValueConsumer) consumer);
			}
			Object value = readValue(this, consumer);
			if (value instanceof YamlParser) {
				YamlParser next = (YamlParser) value;
				next.parent = this;
				
				return next;
			}
			
			
			return findNext();
		}
		
		protected String fieldName() throws IOException, YamlParseException {
			if (fieldName != null) {
				String result = fieldName;
				fieldName = null;
				return result;
			}
			return super.fieldName();
		}

		private MapAdapterYamlParser mapParser(String fieldName) throws IOException, YamlParseException {
			MapAdapter mapAdapter = classInfo.getMapAdapter(fieldName);
			if (mapAdapter != null) {
				return new MapAdapterYamlParser(this, pojo, mapAdapter);
			}
			return null;
		}

		protected void exit() throws YamlParseException {
			if (consumer != null) {
				try {
					consumer.consume(pojo);
				} catch (Exception e) {
					fail(e);
				}
			}
		}


		private ValueConsumer createValueConsumer(String fieldName) throws YamlParseException {
			Method m = classInfo.adderMethod.get(fieldName);
			if (m != null) {
				return new PojoValueConsumer(pojo, m);
			}
			m = classInfo.setterMethod.get(fieldName);
			
			if (m != null) {
				
				Class<?> valueType = m.getParameterTypes()[0];
				if (Map.class.isAssignableFrom(valueType)) {
					ParameterizedType mapType = (ParameterizedType) valueType.getGenericSuperclass();
					Class<?> objectType = (Class<?>)mapType.getActualTypeArguments()[1];
					try {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Map<String,Object> map = (Map) valueType.newInstance();
						m.invoke(pojo, map);
						return new MapValueConsumer(map, fieldName, objectType);
					} catch (Exception e) {
						throw new YamlParseException(e);
					}
					
				}
				
				return new PojoValueConsumer(pojo, m);
			}

			fail("Class " + pojo.getClass().getSimpleName() + " has no setter or adder for field: " + fieldName);
			return null;
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

	private Object readValue(YamlParser parser, ValueConsumer consumer) throws IOException, YamlParseException {
		
		int c = valueStart();
		Object value = null;
		
		switch (c) {
		case '!' :
			value = typedObject(parser, consumer);
			break;
			
		case '&' : {
			ObjectYamlParser objectParser = new ObjectYamlParser(parser, consumer);
			objectParser.setConsumer(consumer);
			value = objectParser;
			break;
		}
			
			
		case '*' :
			value = objectRef();
			assertLineEnd();
			break;
			
		case '\n' :
			read();
			indentWidth();
			c = peek();
			if (c == '-') {
				if (consumer.isAdder()) {
					value = new CollectionYamlParser(parser, consumer);
				} else {
					try {
						Class<?> valueType = consumer.getValueType();
						Object list = valueType.newInstance();
						PojoValueConsumer listConsumer = new PojoValueConsumer(list, collectionAddMethod(), collectionElementType(valueType));
						value = new CollectionYamlParser(parser, listConsumer);
						consumer.consume(list);
					} catch (Throwable e) {
						fail("Failed to consume with " + consumer, e);
					}
				}
			} else {
				ObjectYamlParser objectParser  = new ObjectYamlParser(parser, consumer);
				objectParser.setConsumer(consumer);
				value = objectParser;
			}
			break;
			
		default :
			Class<?> javaType = consumer.getValueType();
			value = scalarValue(javaType);
			assertLineEnd();
			indentWidth();
			if (!isObjectItem && nextIndentWidth>parser.getIndentWidth() && !(value instanceof ObjectYamlParser)) {
				return new ObjectYamlParser(parser, value, consumer, nextIndentWidth);
			} else if (isObjectItem) {
				parser.setIndentWidth(nextIndentWidth);
			}
			isObjectItem = false;
			break;
			
		}

		if (!(value instanceof YamlParser)) {
			try {
				consumer.consume(value);
			} catch (Exception e) {
				fail("Failed to consume field: " + consumer.toString());
			}
		}
		return value;
	}

	private int peek() throws IOException {
		int c = read();
		unread(c);
		return c;
	}
	
	
	private Class<?> collectionElementType(Class<?> collectionType) {
		ParameterizedType type = (ParameterizedType) collectionType.getGenericSuperclass();
		return(Class<?>) type.getActualTypeArguments()[0];
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

	@Override
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}
	
	static class MapAdapter {
		private Method addMethod;
		private Class<?> valueType;
		private Constructor<?> valueConstructor;
		
		public MapAdapter(Method addMethod, Class<?> valueType, Constructor<?> valueConstructor) {
			this.addMethod = addMethod;
			this.valueType = valueType;
			this.valueConstructor = valueConstructor;
		}
		public Method getAddMethod() {
			return addMethod;
		}
		public Class<?> getValueType() {
			return valueType;
		}
		public Constructor<?> getValueConstructor() {
			return valueConstructor;
		}
		
	}
}
