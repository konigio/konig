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


import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class JsonPathParserTest {
	
	private JsonPathParser parser = new JsonPathParser();

	@Test
	public void testField() throws Exception {
		
		JsonPath path = parse("$.givenName");
		assertEquals(2, path.size());
		
		assertTrue(path.get(0) instanceof JsonPathRoot);
		assertTrue(path.get(1) instanceof JsonPathField);
		JsonPathField field = (JsonPathField) path.get(1);
		
		assertTrue(field.getFieldName() instanceof JsonPathName);
		JsonPathName name = (JsonPathName) field.getFieldName();
		assertEquals("givenName", name.getValue());
		
	}
	
	@Test
	public void testNestedField() throws Exception {
		JsonPath path = parse("$.address.postalCode");
		assertEquals(3, path.size());
		
		assertTrue(path.get(0) instanceof JsonPathRoot);
		assertTrue(path.get(1) instanceof JsonPathField);
		assertTrue(path.get(2) instanceof JsonPathField);
		JsonPathField field = (JsonPathField) path.get(2);
		
		assertTrue(field.getFieldName() instanceof JsonPathName);
		JsonPathName name = (JsonPathName) field.getFieldName();
		assertEquals("postalCode", name.getValue());
	}
	
	@Test
	public void testBracketWildcard() throws Exception {
		JsonPath path = parse("$.address[*].postalCode");
		assertEquals(4, path.size());
		
		assertTrue(path.get(0) instanceof JsonPathRoot);
		assertTrue(path.get(1) instanceof JsonPathField);
		assertTrue(path.get(2) instanceof JsonPathBracket);
		assertTrue(path.get(3) instanceof JsonPathField);

		JsonPathBracket bracket = (JsonPathBracket) path.get(2);
		JsonPathKey key = bracket.getKey();
		
		assertTrue(key instanceof JsonPathWildcard);
		
		JsonPathField field = (JsonPathField) path.get(3);
		
		assertTrue(field.getFieldName() instanceof JsonPathName);
		JsonPathName name = (JsonPathName) field.getFieldName();
		assertEquals("postalCode", name.getValue());
	}
	
	protected JsonPath parse(String text) throws JsonPathParseException, IOException {
		StringReader reader = new StringReader(text);
		return parser.parse(reader);
	}

}
