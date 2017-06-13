package io.konig.yaml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class Yaml {

	public static void write(Writer out, Object object) {
		YamlWriter yaml = new YamlWriter(out);
		yaml.write(object);
	}

	public static void write(OutputStream out, Object object) {
		OutputStreamWriter writer = new OutputStreamWriter(out);
		YamlWriter yaml = new YamlWriter(writer);
		yaml.write(object);
	}
	
	public static String toString(Object object) {
		StringWriter buffer = new StringWriter();
		write(buffer, object);
		return buffer.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T read(Class<T> type, Reader reader) throws YamlParseException, IOException {
		YamlReader yaml = new YamlReader(reader);
		return (T) yaml.readObject();
	}
	
	public static <T> T read(Class<T> type, String yamlText) throws YamlParseException, IOException {
		return read(type, new StringReader(yamlText));
	}

}
