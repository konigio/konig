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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class Yaml {

	
	public static void write(File outFile, Object object) throws IOException {
		outFile.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(outFile)) {
			write(writer, object);
		}
	}
	
	public static void write(Writer out, Object object) {
		@SuppressWarnings("resource")
		YamlWriter yaml = new YamlWriter(out);
		yaml.write(object);
	}

	public static void write(Writer out, YamlWriterConfig config, Object object) {
		@SuppressWarnings("resource")
		YamlWriter yaml = new YamlWriter(out);
		yaml.configure(config);
		yaml.write(object);
	}

	public static void write(OutputStream out, Object object) {
		OutputStreamWriter writer = new OutputStreamWriter(out);
		@SuppressWarnings("resource")
		YamlWriter yaml = new YamlWriter(writer);
		yaml.write(object);
	}
	
	public static String toString(Object object) {
		StringWriter buffer = new StringWriter();
		write(buffer, object);
		return buffer.toString();
	}
	
	public static String toString(YamlWriterConfig config, Object object) {
		StringWriter buffer = new StringWriter();
		write(buffer, config, object);
		return buffer.toString();
	}

	
	public static <T> T read(Class<T> type, InputStream input) throws YamlParseException, IOException {
		InputStreamReader reader = new InputStreamReader(input);
		return read(type, reader);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T read(Class<T> type, Reader reader) throws YamlParseException, IOException {
		@SuppressWarnings("resource")
		YamlReader yaml = new YamlReader(reader);
		return (T) yaml.readObject(type);
	}
	
	public static <T> T read(Class<T> type, File file) throws YamlParseException, IOException {
		try (FileReader reader = new FileReader(file)) {
			return read(type, reader);
		}
	}
	
	public static <T> T read(Class<T> type, String yamlText) throws YamlParseException, IOException {
		return read(type, new StringReader(yamlText));
	}

}
