package io.konig.runtime.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerator;

public interface JsonWriter {

	void write(Object data, Writer writer) throws ValidationException, IOException;
	void write(Object data, OutputStream out) throws ValidationException, IOException;
	void write(Object data, JsonGenerator generator) throws ValidationException, IOException;
}
