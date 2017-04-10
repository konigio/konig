package io.konig.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.fasterxml.jackson.core.JsonParser;

public interface JsonReader<T> {

	T read(Reader writer) throws ValidationException, IOException;
	T read(InputStream in) throws ValidationException, IOException;
	T read(JsonParser parser) throws ValidationException, IOException;
}
