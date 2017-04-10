package io.konig.runtime.io;

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
