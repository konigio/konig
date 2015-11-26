package io.konig.core.io;

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
