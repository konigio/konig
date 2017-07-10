package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;

import io.konig.core.util.SimpleValueMap;
import io.konig.core.util.ValueMap;

public class FunctionParser {
	
	private FunctionVisitor visitor;
	private PushbackReader reader;
	private StringBuilder buffer;

	public FunctionParser(FunctionVisitor visitor) {
		this.visitor = visitor;
	}
	
	public void parse(String text) throws FunctionParseException {
		StringReader reader = new StringReader(text);
		parse(reader);
	}
	
	public void parse(Reader reader) throws FunctionParseException {
		this.reader = (reader instanceof PushbackReader) ? (PushbackReader) reader : new PushbackReader(reader);
		try {
			doParse();
		} catch (IOException e) {
			throw new FunctionParseException(e);
		}
	}
	
	private void doParse() throws IOException, FunctionParseException {
		
		int c = peek();
		while (c != -1) {
			function();
			c = peek();
		}
		
	}

	private int peek() throws IOException {
		int c = next();
		unread(c);
		return c;
	}

	private void function() throws IOException, FunctionParseException {
		
		String name = name();
		ValueMap params = paramList();
		Function func = new Function(name, params);
		if (visitor != null) {
			visitor.visit(func);
		}
		
		
	}

	private ValueMap paramList() throws IOException, FunctionParseException {

		SimpleValueMap map = new SimpleValueMap();
		int c = next();
		if (c == '(') {
			
			for (;;) {
				
				String name = name();
				next(':');
				String value = string();
				map.put(name, value);
				c = next();
				if (c == ')') {
					break;
				} 
				if (c != ',') {
					throw new FunctionParseException();
				}
			}
		} else {
			unread(c);
		}
		return map;
	}

	private String string() throws IOException, FunctionParseException {
		int c = next();
		if (c != '"') {
			throw new FunctionParseException();
		}
		buffer = new StringBuilder();
		for (;;) {
			c = read();
			if (c == '"') {
				break;
			}
			buffer.appendCodePoint(c);
		}
		return text();
	}

	private void next(int c) throws FunctionParseException, IOException {
		
		int k = next();
		if (k != c) {
			throw new FunctionParseException();
		}
		
	}

	private String name() throws IOException, FunctionParseException {
		
		buffer = new StringBuilder();
		
		int c = next();
		while (isNameChar(c)) {
			buffer.appendCodePoint(c);
			c = read();
		}
		unread(c);
		
		String name = text();
		if (name.length() == 0) {
			throw new FunctionParseException("Expected a name");
		}
		
		return name;
	}

	private String text() {
		String text = buffer.toString();
		buffer = null;
		return text;
	}

	private void unread(int c) throws IOException {
		if (reader != null) {
			reader.unread(c);
		}
	}

	private boolean isNameChar(int c) {
		
		return 
			(c>='a' && c<='z') ||
			(c>='A' && c<='Z') ||
			(c>='0' && c<='9');
	}

	protected int next() throws IOException {
		int c = read();
		
		while (isWhitespace(c)) {
			c = read();
		}
		
		return c;
		
	}

	protected boolean isWhitespace(int c) {
		return c==' ' || c=='\t' || c=='\r' || c=='\n';
	}
	private int read() throws IOException {
		if (reader == null) {
			return -1;
		}
		int result = reader.read();
		if (result == -1) {
			reader = null;
		} 
		
		return result;
	}

}
