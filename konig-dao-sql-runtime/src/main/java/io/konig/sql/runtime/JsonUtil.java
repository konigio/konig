package io.konig.sql.runtime;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

	public static <T> void write(T object, Writer out) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.writerWithDefaultPrettyPrinter().writeValue(out, object);
	}
	
	public static <T> String toString(T object) {
		StringWriter buffer = new StringWriter();
		try {
			write(object, buffer);
			buffer.flush();
			return buffer.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T read(Class<T> type, Reader reader) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(reader, type);
	}
	
	public static <T> T readString(Class<T> type, String value) throws IOException {
		StringReader reader = new StringReader(value);
		return read(type, reader);
	}

}
