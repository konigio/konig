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
import java.io.InputStream;
import java.io.Reader;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public abstract class BaseJsonReader<T> implements JsonReader<T> {

	public BaseJsonReader() {
	}

	@Override
	public T read(Reader writer) throws ValidationException, IOException {
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(writer);
		return read(parser);

	}

	@Override
	public T read(InputStream in) throws ValidationException, IOException {

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(in);
		return read(parser);

	}
	
	protected void skipField(JsonParser parser) throws IOException {
	
		switch (parser.nextToken()) {
		case START_OBJECT :
			skipObject(parser);
			break;
			
		case START_ARRAY :
			skipArray(parser);
			break;
			
		default: // Do nothing
		}
	}
	
	private void skipArray(JsonParser parser) throws JsonParseException, IOException {
		
		while (!parser.isClosed() && parser.nextToken() != JsonToken.END_ARRAY) {
			switch (parser.getCurrentToken()) {
			case START_OBJECT :
				skipObject(parser);
				break;
				
			case START_ARRAY :
				skipArray(parser);
				break;
				
			default: // Do nothing
			}
			
		}
		
	}

	private void skipObject(JsonParser parser) throws JsonParseException, IOException {
		
		while (!parser.isClosed() && parser.nextToken() == JsonToken.FIELD_NAME) {
			
			switch (parser.nextToken()) {
			case START_OBJECT :
				skipObject(parser);
				break;
				
			case START_ARRAY :
				skipArray(parser);
				break;
				
			default: // Do nothing
			}
		}
		
	}

	protected void assertStartArray(JsonParser parser) throws IOException {
		if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
			throw new JsonParseException("Expected an array", parser.getTokenLocation());
		}
	}
	
	protected GregorianCalendar dateTime(JsonParser parser) throws IOException {
		DateTimeFormatter fmt = ISODateTimeFormat.dateOptionalTimeParser();
		String text = parser.getValueAsString();
		DateTime joda = fmt.parseDateTime(text);
		return joda.toGregorianCalendar();
	}
	


}
