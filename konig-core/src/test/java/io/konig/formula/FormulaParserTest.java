package io.konig.formula;

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

import org.junit.Test;

public class FormulaParserTest {
	
	private FormulaParser parser = new FormulaParser();


	@Test
	public void testConditional() throws Exception {

		String text = "(sprintIssue.status = pmd:Complete) ? sprintIssue.timeEstimate : 0";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testCurieTerm() throws Exception {

		String text = "ex:alpha.ex:beta NOT IN (ex:foo , ex:bar)";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	
	@Test
	public void testIriTerm() throws Exception {

		String text = "<http://example.com/alpha>.beta NOT IN (foo , bar)";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testNotIn() throws Exception {

		String text = "alpha.beta NOT IN (foo , bar)";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testIn() throws Exception {

		String text = "alpha.beta IN (foo , bar)";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testEquals() throws Exception {

		String text = "alpha.beta = one.two";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testNumberPlusNumber() throws Exception {
		
		String text = "2.5 + 3";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
		
	}
	
	@Test
	public void testPathPlusNumber() throws Exception {

		String text = "address.streetAddress + 3";
		
		Expression e = parser.parse(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testBracket() throws Exception {

		String text = "(owner.age + 3*7)/(owner.weight*4)";
		
		Expression e = parser.parse(text);
		
		String expected = "(owner.age + 3 * 7) / (owner.weight * 4)";
		String actual = e.toString();
	
		assertEquals(expected, actual);
	}

}
