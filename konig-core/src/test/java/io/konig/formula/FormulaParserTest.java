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


import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import io.konig.core.vocab.Schema;

public class FormulaParserTest {
	
	private FormulaParser parser = new FormulaParser();
	

	
	
	@Test
	public void testSum() throws Exception {
		String text = 
			"@context {\n" + 
			"   \"price\" : \"http://schema.org/price\",\n" + 
			"   \"OfferShape\" : \"http://example.com/ns/OfferShape\",\n" + 
			"   \"hasShape\" : \"http://www.konig.io/ns/core/hasShape\"\n" + 
			"}\n" + 
			"SUM(?x.price)\n" + 
			"WHERE\n" + 
			"   ?x hasShape OfferShape .\n" + 
			"";
		
		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		String expected = text;
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testIfPlus() throws Exception {
		String text = 
			"IF(reviewer0Approved, 1, 0)\n" + 
			"+ IF(reviewer1Approved, 1, 0)\n" + 
			"+ IF(reviewer2Approved, 1, 0)\n" + 
			"+ IF(reviewer3Approved, 1, 0)";

		Expression e = parser.expression(text);
		String actual = e.toString();
		
		String expected = "IF(reviewer0Approved , 1 , 0) + IF(reviewer1Approved , 1 , 0) + IF(reviewer2Approved , 1 , 0) + IF(reviewer3Approved , 1 , 0)";
		assertEquals(expected, actual);
	}
	
	@Test
	public void testIfFunction() throws Exception {
		
		String text = "@context {\n" + 
			"   \"email\" : \"http://schema.org/email\"\n" + 
			"}\n" + 
			"IF(email , 1 , 0)";
		
		Expression e = parser.expression(text);
		String actual = e.toString();
		assertEquals(text, actual);
	}
	
	@Test
	public void testNotEquals() throws Exception {
		String text = 
			"@context {\n" + 
			"   \"created\" : \"http://www.konig.io/ns/core/created\",\n" + 
			"   \"modified\" : \"http://www.konig.io/ns/core/modified\"\n" + 
			"}\n" + 
			"created != modified";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testContext() throws Exception {
		
		String text =
			  "@prefix schema: <http://schema.org/> .\n"
			+ "@context {\n"
			+ "   \"knows\" : \"schema:knows\",\n"
			+ "   \"Alice\" : {\n"
			+ "      \"@id\" : \"http://example.com/Alice\",\n"
			+ "      \"@type\" : \"@vocab\"\n"
			+ "   }\n"
			+ "}\n"
			+ "knows.knows = Alice";

		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
		
		ConditionalAndExpression and = e.getOrList().iterator().next();
		
		BinaryRelationalExpression binary = (BinaryRelationalExpression) and.getAndList().iterator().next();
		
		GeneralAdditiveExpression left = (GeneralAdditiveExpression) binary.getLeft();
		
		PathExpression path = (PathExpression) left.getLeft().getLeft().getPrimary();
		PathTerm term = path.getStepList().get(0).getTerm();
		
		assertEquals(Schema.knows, term.getIri());
	}

	@Test
	public void testConditional() throws Exception {

		String text = "(sprintIssue.status = pmd:Complete) ? sprintIssue.timeEstimate : 0";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testCurieTerm() throws Exception {

		String text = "ex:alpha.ex:beta NOT IN (ex:foo , ex:bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	
	@Test
	public void testIriTerm() throws Exception {

		String text = "<http://example.com/alpha>.beta NOT IN (foo , bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testNotIn() throws Exception {

		String text = "alpha.beta NOT IN (foo , bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testIn() throws Exception {

		String text = "alpha.beta IN (foo , bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testEquals() throws Exception {

		String text = "alpha.beta = one.two";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testNumberPlusNumber() throws Exception {
		
		String text = "2.5 + 3";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
		
	}
	
	@Test
	public void testPathPlusNumber() throws Exception {

		String text = "address.streetAddress + 3";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testBracket() throws Exception {

		String text = "(owner.age + 3*7)/(owner.weight*4)";
		
		Expression e = parser.expression(text);
		
		String expected = "(owner.age + 3 * 7) / (owner.weight * 4)";
		String actual = e.toString();
	
		assertEquals(expected, actual);
	}

}
