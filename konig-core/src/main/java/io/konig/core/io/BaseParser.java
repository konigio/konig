package io.konig.core.io;

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
import java.io.PushbackReader;
import java.io.Reader;

public class BaseParser {

	protected PushbackReader reader;
	protected StringBuilder buffer = new StringBuilder();
	private String baseURI;
	private int lineNumber=1;
	private int columnNumber;
	
	protected BaseParser(Reader reader, int maxBuffer) {
		this.reader = new PushbackReader(reader, maxBuffer);
	}

	protected StringBuilder buffer() {
		buffer.setLength(0);
		return buffer;
	}
	
	protected int read() throws IOException {
		if (reader == null) {
			return -1;
		}
		int next = reader.read();
		if (next == -1) {
			reader = null;
			return -1;
		}
		columnNumber++;
		if (Character.isHighSurrogate((char) next)) {
			next = Character.toCodePoint((char)next, (char) reader.read());
			columnNumber++;
		}

		if (next == '\n') {
			lineNumber++;
			columnNumber = 0;
		}
		return next;
	}

	protected boolean skipSpace() throws IOException {
		boolean result = false;
		int c = read();
		while (isWhitespace(c)) {
			result = true;
			c = read();
		}
		unread(c);
		
		return result;
		
	}

	protected boolean isWhitespace(int c) {
		return c==' ' || c=='\t' || c=='\r' || c=='\n';
	}

	protected void unread(int codePoint) throws IOException {
		if (codePoint != -1) {
			if (Character.isSupplementaryCodePoint(codePoint)) {
				final char[] surrogatePair = Character.toChars(codePoint);
				reader.unread(surrogatePair);
				columnNumber -= 2;
			} else {
				if (codePoint=='\n') {
					lineNumber--;
					columnNumber=Integer.MIN_VALUE;
				} else {
					columnNumber--;
				}
				reader.unread(codePoint);
			}
		}
	}

	
	protected int peek() throws IOException {
		int c = read();
		unread(c);
		return c;
	}
}
