package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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
