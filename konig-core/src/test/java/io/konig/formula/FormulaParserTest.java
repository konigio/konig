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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.vocab.Schema;

public class FormulaParserTest {
	
	private FormulaParser parser = new FormulaParser();
	

	@Test
	public void testDistinct() throws Exception {
		String text = 
			"@term dateCreated <http://schema.org/dateCreated>\n" +
			"COUNT( DISTINCT .dateCreated )";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof SetFunctionExpression);
	
		SetFunctionExpression function = (SetFunctionExpression) primary;
		assertEquals("COUNT", function.getFunctionName());
		assertTrue(function.isDistinct());
	}
	
	@Test
	public void testUnixTime() throws Exception {
		String text = 
			"@term dateCreated <http://schema.org/dateCreated>\n" +
			"UNIX_TIME(.dateCreated)";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof FunctionExpression);
	
		FunctionExpression function = (FunctionExpression) primary;
		assertEquals("UNIX_TIME", function.getFunctionName());
	}
	
	
	@Test
	public void testTerm() throws Exception {
		String text = 
			"@term address <http://schema.org/address>\n" +
			"@prefix schema : <http://schema.org/> .\n" +
			"@term postalCode schema:postalCode\n" +
			".address.postalCode";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		Context context = e.getContext();
		context.compile();
		
		Term term = context.getTerm("address");
		assertEquals(Schema.address, term.getExpandedId());
		
		term = context.getTerm("postalCode");
		assertEquals(Schema.postalCode, term.getExpandedId());
		
		String actualText = e.toString();
		String expectedText = 
				"@prefix schema: <http://schema.org/> .\n" + 
				"@term address <http://schema.org/address>\n" + 
				"@term postalCode schema:postalCode\n" + 
				"\n" + 
				".address.postalCode";
		
		assertEquals(expectedText, actualText);
	}
	
	@Test
	public void testTimeInterval() throws Exception {

		String text = 
			"@term endTime <http://schema.org/endTime>\n" + 
			"@term Day <http://www.konig.io/ns/core/Day>\n" + 
			"@term Week <http://www.konig.io/ns/core/Week>\n" + 
			"@term Month <http://www.konig.io/ns/core/Month>\n\n" + 
			"TIME_INTERVAL(?x.endTime, Day, Week, Month)";

		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		String expected = text;
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testCountFormula() throws Exception {

		String text = 
			"@term price <http://schema.org/price>\n\n" + 
			"COUNT(?x.price)";

		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		String expected = text;
		assertEquals(expected, actual);
		
	}
	

	@Test
	public void testOutPathStartingWithE() throws Exception {
		
		String text = ".estimatedPoints";
		
		Expression e = parser.quantifiedExpression(text);
		
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof PathExpression);
		
		assertEquals(text, primary.toString());
		
	}
	
	
	@Test
	public void testPathValue() throws Exception {
		
		String text = "<http://example.com/foo>";
		
		Expression e = parser.quantifiedExpression(text);
		PrimaryExpression p = e.asPrimaryExpression();
		assertTrue(p instanceof IriValue);
		
	}
	
	@Test
	public void testHasStep() throws Exception {
		String text = 
			"@term address <http://schema.org/address>\n" + 
			"@term addressCountry <http://schema.org/addressCountry>\n" + 
			"@term addressRegion <http://schema.org/addressRegion>\n\n" + 
			"address[addressCountry \"US\"; addressRegion \"VA\"]";
		
		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		assertEquals(text, actual);
	}

	@Test
	public void testBound() throws Exception {
		String text = 
			"@term price <http://schema.org/price>\n\n" + 
			"BOUND(price)" ;
		
		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		String expected = text;
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testSum() throws Exception {
		String text = 
			"@term price <http://schema.org/price>\n" + 
			"@term OfferShape <http://example.com/ns/OfferShape>\n" + 
			"@term hasShape <http://www.konig.io/ns/core/hasShape>\n\n" +
			
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
		
		String text = 
			"@term email <http://schema.org/email>\n\n" + 
			"IF(email , 1 , 0)";
		
		Expression e = parser.expression(text);
		String actual = e.toString();
		assertEquals(text, actual);
	}
	
	@Test
	public void testNotEquals() throws Exception {
		String text = 
			"@term created <http://www.konig.io/ns/core/created>\n" + 
			"@term modified <http://www.konig.io/ns/core/modified>\n" + 
			"\n" + 
			"created != modified";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testContext() throws Exception {
		
		String text =
			  "@prefix schema: <http://schema.org/> .\n" + 
			  "@term knows schema:knows\n" + 
			  "@term Alice <http://example.com/Alice>\n\n"
			+ ".knows.knows = Alice";

		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
		
		ConditionalAndExpression and = e.getOrList().iterator().next();
		
		BinaryRelationalExpression binary = (BinaryRelationalExpression) and.getAndList().iterator().next();
		
		GeneralAdditiveExpression left = (GeneralAdditiveExpression) binary.getLeft();
		
		PathExpression path = (PathExpression) left.getLeft().getLeft().getPrimary();
		DirectionStep step = (DirectionStep) path.getStepList().get(0);
		PathTerm term = step.getTerm();
		
		assertEquals(Schema.knows, term.getIri());
	}

	@Test
	public void testConditional() throws Exception {

		String text = 
				"@term pmd <http://example.com/pmd>\n\n" + 
				"(sprintIssue.status = pmd:Complete) ? sprintIssue.timeEstimate : 0";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testCurieTerm() throws Exception {

		String text = 
				"@term ex <http://example.com/core>\n\n" + 
				"ex:alpha.ex:beta NOT IN (ex:foo , ex:bar)";
		
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
