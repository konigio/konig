package io.konig.runtime.io;

/*
 * #%L
 * Konig Java
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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public abstract class BaseJsonWriter implements JsonWriter {


	@Override
	public void write(Object data, Writer writer) throws ValidationException, IOException {
		
		JsonFactory factory = new JsonFactory();

		JsonGenerator json = factory.createGenerator(writer);
		json.useDefaultPrettyPrinter();
		write(data, json);
		json.flush();
	}

	@Override
	public void write(Object data, OutputStream out) throws ValidationException, IOException {

		JsonFactory factory = new JsonFactory();

		JsonGenerator json = factory.createGenerator(out);
		json.useDefaultPrettyPrinter();
		write(data, json);
		json.flush();

	}
	

	
	@SuppressWarnings("unchecked")
	protected <T> T value(Object object, Class<T> type) {
		if (object instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Collection c = (Collection)object;
			if (c != null  && !c.isEmpty()) {
				return (T)c.iterator().next();
			}
		} 
		return (T) object;
		
	}

}
