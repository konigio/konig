package io.konig.runtime.io;

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
