package io.konig.core.util;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import java.util.List;

import org.junit.Test;

import io.konig.core.Context;
import io.konig.core.ContextBuilder;
import io.konig.core.Term;

public class ContextExtenderTest {

	@Test
	public void testExtend() throws Exception {
		
		
		Context a = new ContextBuilder("http://example.com/context/a")
			.term("schema", "http://schema.org/")
			.term("Person", "schema:Person")
			.term("givenName", "schema:givenName")
			.term("familyName", "schema:familyName")
			.getContext();
		
		Context b = new ContextBuilder("http://example.com/context/b")
				.term("schema", "http://schema.org/")
				.term("Organization", "schema:Organization")
				.term("duns", "schema:duns")
				.term("givenName", "schema:givenName")
				.term("familyName", "schema:familyName")
				.term("Person", "schema:Person")
				.getContext();
			;
		
		
		ContextExtender extender = new ContextExtender();
		Context c = extender.extend(a, b);
		c.compile();
		
		List<Term> aList = a.asList();
		List<Term> cList = c.asList();
		
		assertEquals(6, cList.size());
		
		for (int i=0; i<aList.size(); i++) {
			Term aTerm = aList.get(i);
			Term cTerm = cList.get(i);
			
			assertCompatible(aTerm, cTerm);
			
		}
		
		Term org = cList.get(4);
		assertEquals("Organization", org.getKey());
		assertEquals("http://schema.org/Organization", org.getExpandedIdValue());
		
		Term duns = cList.get(5);
		assertEquals("duns", duns.getKey());
		
		
	}

	private void assertCompatible(Term a, Term b) {
		assertTrue(a!=null);
		assertTrue(b!=null);
		
		assertEquals(a.getExpandedId(), b.getExpandedId());
		assertEquals(a.getKey(), b.getKey());
		
	}

}
