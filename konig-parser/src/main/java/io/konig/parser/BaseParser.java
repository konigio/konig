package io.konig.parser;

/*
 * #%L
 * Konig Parser
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

/**
 * An abstract base class used to implement a Parser.
 * This class contains methods that are useful for parsing.
 * <p>
 * The typical usage pattern is to define a sub-class like this...
 * <pre>
 * public class MyParser extends BaseParser {
 * 
 *   public MyParser() {
 *     super(20); // Adjust the lookAheadLimit to meet your needs
 *   }
 *   
 *   public SomePojo parse(Reader reader) throws IOException, ParseException {
 *     initParse(reader);
 *     SomePojo pojo = new SomePojo();
 *     
 *     //  Add your parsing logic here.
 *     
 *     return pojo;
 *   }
 * }
 * 
 * </pre>
 * </p>
 * 
 * @author Greg McFall
 *
 */
public abstract class BaseParser {

	protected PushbackReader reader;
	private StringBuilder buffer = new StringBuilder();
	protected int lineNumber=1;
	protected int columnNumber;
	protected int lookAheadLimit;

	/**
	 * Skip over whitespace.
	 * After this call, the next call to the read() method will return the end-of-stream signal (-1) or a non-whitespace character.
	 * @return true if the parser skipped any whitespace and false otherwise.
	 * @throws IOException
	 */
	protected boolean tryWhitespace() throws IOException {
		boolean result = false;
		int c = read();
		while (isWhitespace(c)) {
			result = true;
			c = read();
		}
		unread(c);
		
		return result;
		
	}

	/**
	 * Skip any whitespace, and then read the next code point.
	 * @return The next code point after any whitespace, or -1 if the end of stream has been reached.
	 * @throws IOException
	 */
	protected int next() throws IOException {
		int c = read();
		
		while (isWhitespace(c)) {
			c = read();
		}
		
		return c;
		
	}
	
	
	/**
	 * Assert that the next code point is a whitespace character, and skip over any subsequent whitespace characters.
	 * @throws IOException If a error occurred while reading the next code point.
	 * @throws ParseException If the next code point is not a whitespace character.
	 */
	protected void assertWhitespace() throws IOException, ParseException {
		int c = read();
		if (!isWhitespace(c)) {
			fail("Expected whitespace");
		}
		tryWhitespace();
	}
	

	/**
	 * Get an empty StringBuilder that will be maintained internally.  This is useful because it is 
	 * more efficient to create a StringBuilder once and then reuse it multiple times by truncating its contents.
	 * You can get this StringBuilder later by calling the buffer() method.
	 * @return
	 */
	protected StringBuilder newBuffer() {
		buffer.setLength(0);
		return buffer;
	}
	
	/**
	 * Skip over any whitespace, read the next code point, and assert that it matches the expected value.
	 * @param expected The expected code point value
	 * @throws IOException If there was a problem reading the next code point.
	 * @throws ParseException If the next, non-whitespace code point does not match the expected value
	 */
	protected void assertNext(int expected) throws IOException, ParseException {
		int actual = next();
		assertEquals(expected, actual);
	}
	
	/**
	 * Assert that a certain expected code point matches the actual code point encountered.
	 * @param expected The expected code point
	 * @param actual The actual code point
	 * @throws ParseException If the expected code point does not match the actual code point.
	 */
	protected void assertEquals(int expected, int actual) throws ParseException {
		
		if (actual != expected) {
			StringBuilder builder = newBuffer();
			builder.append("Expected '");
			builder.appendCodePoint(expected);
			builder.append("' but found '");
			builder.append(actual);
			builder.append("'");
			fail(builder.toString());
		}
		
		
	}

	
	/**
	 * Get the internal StringBuilder.
	 * Use newBuffer() instead, if you want to ensure that the StringBuilder is empty.
	 * @return The internally defined StringBuilder.  
	 */
	protected StringBuilder buffer() {
		return buffer;
	}
	

	/**
	 * Look ahead at the next code point in the reader without
	 * advancing the position of the reader.	
	 * @return The next code point, or -1 if the reader has reached the end of the stream.
	 * @throws IOException
	 */
	protected int peek() throws IOException {
		int c = read();
		unread(c);
		return c;
	}
	
	/**
	 * Assert that the reader is positioned at a given expected token.
	 * This method does NOT skip any leading whitespace.
	 * @param expected The expected lexical token.
	 * @throws IOException If an error is encountered while reading the next token.
	 * @throws ParseException If the reader is not positioned at the expected lexical token.
	 */
	protected void assertToken(String expected) throws IOException, ParseException {
		for (int i=0; i<expected.length();) {
			int c = expected.codePointAt(i);
			i += Character.charCount(c);
			int d = read();
			if (c != d) {
				StringBuilder buffer = newBuffer();
				buffer.append("Expected '");
				buffer.append(expected);
				buffer.append("' but found: ");
				for (int j=0; j<i;) {
					c = expected.codePointAt(j);
					buffer.appendCodePoint(c);
					j += Character.charCount(c);
				}
				buffer.appendCodePoint(d);
				buffer.append("...");
				fail(buffer.toString());
			}
		}
	}

	/**
	 * Assert that the reader is positioned at a given expected token without regard to case.
	 * This method does NOT skip any leading whitespace.
	 * @param expected The expected lexical token.
	 * @throws IOException If an error is encountered while reading the next token.
	 * @throws ParseException If the reader is not positioned at the expected lexical token.
	 */
	protected void assertTokenIgnoreCase(String expected) throws IOException, ParseException {
		for (int i=0; i<expected.length();) {
			int c = expected.codePointAt(i);
			i += Character.charCount(c);
			int d = read();
			if (Character.toLowerCase(c) != Character.toLowerCase(d)) {
				StringBuilder buffer = newBuffer();
				buffer.append("Expected '");
				buffer.append(expected);
				buffer.append("' but found: ");
				for (int j=0; j<i;) {
					c = expected.codePointAt(j);
					buffer.appendCodePoint(c);
					j += Character.charCount(c);
				}
				buffer.appendCodePoint(d);
				buffer.append("...");
				fail(buffer.toString());
			}
		}
	}

	/**
	 * Test whether the reader is positioned at the beginning of a specific lexical token.
	 * If the reader is positioned at the beginning of the specified token, then when this method exits
	 * the reader will be positioned at the end of the token.  Otherwise the reader position will
	 * not change.  This method does NOT skip over any leading whitespace.
	 * @param token The token to be evaluated.
	 * @return true if the reader was positioned at the beginning of the specified token, and false otherwise.
	 * @throws IOException
	 */
	protected boolean tryToken(String token) throws IOException {
		for (int i=0; i<token.length(); i++) {
			char c = token.charAt(i);
			int k = read();
			if (k != c) {
				unread(k);
				for (int j=i-1; j>=0; j--) {
					unread(token.charAt(j));
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Test whether the reader is positioned at the beginning of a specific lexical token, without regard to case.
	 * If the reader is positioned at the beginning of the specified token, then when this method exits
	 * the reader will be positioned at the end of the token.  Otherwise the reader position will
	 * not change.  This method does NOT skip over any leading whitespace.
	 * @param token The token to be evaluated.
	 * @return true if the reader was positioned at the beginning of the specified token, and false otherwise.
	 * @throws IOException
	 */
	protected boolean tryTokenIgnoreCase(String token) throws IOException {
		for (int i=0; i<token.length(); i++) {
			char c = token.charAt(i);
			int k = read();
			if (Character.toLowerCase(k) != Character.toLowerCase(c)) {
				unread(k);
				for (int j=i-1; j>=0; j--) {
					unread(token.charAt(j));
				}
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Create a new BaseParser.
	 * @param lookAheadLimit  The maximum number of characters that this parser can lookahead while parsing. 
	 */
	public BaseParser(int lookAheadLimit) {
		this.lookAheadLimit = lookAheadLimit;
	}
	
	/**
	 * Test whether a given code point is white space.
	 * @param c The code point to be tested
	 * @return True if the given code point is white space and false otherwise.
	 */
	protected boolean isWhitespace(int c) {
		return c==' ' || c=='\t' || c=='\r' || c=='\n';
	}

	/**
	 * Throw a ParseException whose message includes the current lineNumber and columnNumber
	 * where the parse error occurred.
	 * @param message The message to include in the ParseException.  This message will be prefixed with a 
	 * string of the form 'At (lineNumber:columnNumber) -'
	 * @throws ParseException
	 */
	protected void fail(String message) throws ParseException {
		StringBuilder builder = new StringBuilder();
		builder.append("At (");
		builder.append(lineNumber);
		builder.append(':');
		builder.append(columnNumber);
		builder.append(") - ");
		builder.append(message);
		throw new ParseException(builder.toString());
	}
	
	/**
	 * Push the given code point back into the internal reader and decrement the 
	 * line number and column number counters if necessary.
	 * @param codePoint The code point to be pushed back into the internal reader
	 * @throws IOException
	 */
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

	/**
	 * Wrap the given reader in a PushbackReader.
	 * This method must be called before you begin parsing.
	 * @param reader The reader to be wrapped.
	 */
	protected void initParse(Reader reader) {
		if (this.reader != reader) {
			this.reader = new PushbackReader(reader, lookAheadLimit);
		}
	}
	
	/**
	 * Read the next code point and increment the internal line and column counters.
	 * @return The next code point or -1 if the end of the stream has been reached.
	 * @throws IOException
	 */
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

}
