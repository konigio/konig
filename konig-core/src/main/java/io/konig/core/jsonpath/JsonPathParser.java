package io.konig.core.jsonpath;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import io.konig.core.io.BaseParser;

public class JsonPathParser {
	
	public JsonPath parse(Reader input) throws JsonPathParseException, IOException {
		Worker worker = new Worker(input);
		
		return worker.parse();
	}
	
	private static class Worker extends BaseParser {

		protected Worker(Reader reader) {
			super(reader, 8);
		}

		public JsonPath parse() throws IOException, JsonPathParseException {
			JsonPath path = new JsonPath();
			for (JsonPathOperator jop = tryOperator(); jop!=null; jop = tryOperator()) {
				path.add(jop);
			}
			return path;
		}

		private JsonPathOperator tryOperator() throws IOException, JsonPathParseException {
			skipSpace();
			int c = peek();
			switch (c) {
			case '$' : read(); return new JsonPathRoot();
			case '.' : return field();
			case '[' : return bracket();
			}
			return null;
		}

		private JsonPathBracket bracket() throws IOException, JsonPathParseException {
			// Throw away bracket
			read();
			JsonPathKey index = key();
			skipSpace();
			int c = read();
			if (c != ']') {
				throw new JsonPathParseException("Expected ']'");
			}
			return new JsonPathBracket(index);
		}

		private JsonPathOperator field() throws IOException, JsonPathParseException {
			// Throw away '.'
			read();
			JsonPathKey fieldName = key();
			if (fieldName instanceof JsonPathIndex) {
				throw new JsonPathParseException("Expected a name but found an index");
			}
			
			return new JsonPathField(fieldName);
		}

		private JsonPathKey key() throws IOException, JsonPathParseException {
			skipSpace();
			StringBuilder buffer = buffer();
			int c = read();
			for (; c>0; c=read()) {
				if (!isJsonName(c) ) {
					unread(c);
					break;
				}
				buffer.appendCodePoint(c);
			} 
			String text = buffer.toString();
			if (text.length()==0) {
				throw new JsonPathParseException("Expected a name or index");
			}
			if ("*".equals(text)) {
				return new JsonPathWildcard();
			}
			if (Character.isDigit(text.charAt(0))) {
				return new JsonPathIndex(Integer.parseInt(text));
			}
			
			return new JsonPathName(text);
		}

		private boolean isJsonName(int c) {
			
			return Character.isAlphabetic(c) || Character.isDigit(c) || c=='*';
		}
		
	}


}
