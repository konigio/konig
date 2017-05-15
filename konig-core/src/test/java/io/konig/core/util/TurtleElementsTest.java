package io.konig.core.util;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import io.konig.core.impl.BasicContext;
import io.konig.core.vocab.Schema;

public class TurtleElementsTest {

	@Test
	public void testCurie() {
		
		BasicContext context = new BasicContext(null);
		context.addTerm("schema", "http://schema.org/");
		
		
		String expected = "schema:givenName";
		String actual = TurtleElements.iri(context, Schema.givenName);
		
		assertEquals(expected, actual);
	}
	
	@Test 
	public void testLocalName() {
		BasicContext context = new BasicContext(null);
		context.addTerm("schema", "http://schema.org/");
		context.addTerm("firstName", "schema:givenName");
		
		
		String expected = "firstName";
		String actual = TurtleElements.iri(context, Schema.givenName);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFullyQualified() {

		BasicContext context = new BasicContext(null);
		
		String expected = "<http://schema.org/givenName>";
		String actual = TurtleElements.iri(context, Schema.givenName);
		
		assertEquals(expected, actual);
	}

}
