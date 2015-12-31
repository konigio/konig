package io.konig.core.io;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import io.konig.core.Context;
import io.konig.core.Term;

public class ContextWriter {

	public void write(Context context, JsonGenerator json) throws IOException {
		json.writeStartObject();
		json.writeObjectFieldStart("@context");
		for (Term term : context.asList()) {
			write(term, json);
		}
		
		json.writeEndObject();
		json.writeStringField("@id", context.getContextIRI());
		json.writeEndObject();
	}
	
	public void write(Context context, OutputStream out) throws IOException {
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createGenerator(out);
		generator.setPrettyPrinter(new DefaultPrettyPrinter());
		write(context, generator);
		generator.flush();
	}
	
	public void write(Context context, Writer writer) throws IOException {
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createGenerator(writer);
		generator.setPrettyPrinter(new DefaultPrettyPrinter());
		write(context, generator);
	}

	private void write(Term term, JsonGenerator json) throws IOException {

		String key = term.getKey();
		String id = term.getId();
		String language = term.getLanguage();
		String type = term.getType();
		
		if (language==null && type==null) {
			json.writeStringField(key, id);
		} else {
			json.writeObjectFieldStart(key);
			if (id != null) {
				json.writeStringField("@id", id);
			}
			if (language != null) {
				json.writeStringField("@language", language);
			}
			if (type != null) {
				json.writeStringField("@type", type);
			}
			json.writeEndObject();
		}
		
	}

}
