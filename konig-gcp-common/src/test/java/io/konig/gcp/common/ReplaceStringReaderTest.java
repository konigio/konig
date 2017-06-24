package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Common
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


import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Ignore;
import org.junit.Test;

public class ReplaceStringReaderTest {
	
	@Test
	public void testPartialArrayLarge() throws Exception {
		try (
				StringReader source = new StringReader("one foofoo two foo three");
				ReplaceStringReader reader = new ReplaceStringReader(source, "foo", "alpha");
			) {
				StringBuilder builder = new StringBuilder();
				int len;
				char[] buf = new char[7];
				while ( (len=reader.read(buf, 0, 7)) != -1) {
					builder.append(buf, 0, len);
				}
				String text = builder.toString();
				assertEquals("one alphaalpha two alpha three", text);
			
			}
	}
	@Test
	public void testPartialArraySmall() throws Exception {
		try (
				StringReader source = new StringReader("one alphaalpha two alpha three");
				ReplaceStringReader reader = new ReplaceStringReader(source, "alpha", "foo");
			) {
				StringBuilder builder = new StringBuilder();
				int len;
				char[] buf = new char[7];
				while ( (len=reader.read(buf, 0, 7)) != -1) {
					builder.append(buf, 0, len);
				}
				String text = builder.toString();
				assertEquals("one foofoo two foo three", text);
			
			}
	}

	@Test
	public void testFullArraySmall() throws Exception {
		try (
				StringReader source = new StringReader("one alphaalpha two alpha three");
				ReplaceStringReader reader = new ReplaceStringReader(source, "alpha", "foo");
			) {
				StringBuilder builder = new StringBuilder();
				int len;
				char[] buf = new char[50];
				while ( (len=reader.read(buf, 0, 50)) != -1) {
					builder.append(buf, 0, len);
				}
				String text = builder.toString();
				assertEquals("one foofoo two foo three", text);
			
			}
	}
	
	@Test
	public void testFullArrayLarge() throws Exception {
		try (
				StringReader source = new StringReader("one foofoo two foo three");
				ReplaceStringReader reader = new ReplaceStringReader(source, "foo", "alpha");
			) {
				StringBuilder builder = new StringBuilder();
				int len;
				char[] buf = new char[50];
				while ( (len=reader.read(buf, 0, 50)) != -1) {
					builder.append(buf, 0, len);
				}
				String text = builder.toString();
				assertEquals("one alphaalpha two alpha three", text);
			
			}
	}

	@Test
	public void testSingleSmall() throws Exception {
		
		try (
			StringReader source = new StringReader("one alphaalpha two alpha three");
			ReplaceStringReader reader = new ReplaceStringReader(source, "alpha", "foo");
		) {
			StringBuilder builder = new StringBuilder();
			int c;
			while ( (c=reader.read()) != -1) {
				builder.appendCodePoint(c);
			}
			String text = builder.toString();
			assertEquals("one foofoo two foo three", text);
		
		}
	}
	
	@Test
	public void testSingleLarge() throws Exception {
		
		try (
			StringReader source = new StringReader("one foofoo two foo three");
			ReplaceStringReader reader = new ReplaceStringReader(source, "foo", "alpha");
		) {
			StringBuilder builder = new StringBuilder();
			int c;
			while ( (c=reader.read()) != -1) {
				builder.appendCodePoint(c);
			}
			String text = builder.toString();
			assertEquals("one alphaalpha two alpha three", text);
		
		}
	}

}
