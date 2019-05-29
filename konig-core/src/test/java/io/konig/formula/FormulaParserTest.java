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

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;

public class FormulaParserTest {

	private FormulaParser parser = new FormulaParser();
	
	@Test
	public void testInversePath() throws Exception {
		String text = "$^address";

		SimpleLocalNameService service = new SimpleLocalNameService();
		service.add(Schema.address);
		
		FormulaParser parser = new FormulaParser(null, service);

		QuantifiedExpression e = parser.quantifiedExpression(text);
		String actual = e.toSimpleString();
		
		String expected = text;
		
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testIriTemplate() throws Exception {
		String text = "<http://example.com/person/{uid}>";

		SimpleLocalNameService service = new SimpleLocalNameService();
		service.add(Konig.uid);
		


		String expected = "@term uid <http://www.konig.io/ns/core/uid>\n" + 
				"\n" + 
				"<http://example.com/person/{uid}>";
		
		validate(expected, text, new FormulaParser(null, service));
		validate(expected, expected, parser);
		
	}
	
	private void validate(String expected, String text, FormulaParser parser) throws RDFParseException, IOException {

		QuantifiedExpression e = parser.quantifiedExpression(text);
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof IriTemplateExpression);
		
		IriTemplate template = ((IriTemplateExpression)primary).getTemplate();
		assertEquals(2, template.toList().size());
		Element element = template.toList().get(1);
		assertEquals(ElementType.VARIABLE, element.getType());
		assertEquals("uid", element.getText());
		
		String formulaText = e.toString();
		
		
		assertEquals(expected, formulaText.trim());
		
	}

	@Test
	public void testMonth() throws Exception {
		String text = "MONTH($.dateCreated)";

		SimpleLocalNameService service = new SimpleLocalNameService();
		service.add(Schema.dateCreated);
		

		FormulaParser parser = new FormulaParser(null, service);

		QuantifiedExpression e = parser.quantifiedExpression(text);
		String actual = e.toSimpleString();
		
		assertEquals(text, actual);
		
		
	}
	
	@Test
	public void testNamedIndividual() throws Exception {
		String text = "Male";

		SimpleLocalNameService service = new SimpleLocalNameService();
		service.add(Schema.Male);
		

		FormulaParser parser = new FormulaParser(null, service);

		QuantifiedExpression e = parser.quantifiedExpression(text);
		String actual = e.toSimpleString();
		
		PrimaryExpression primary = e.asPrimaryExpression();
		
		assertEquals("io.konig.formula.LocalNameTerm", primary.getClass().getName());
		
		assertEquals("Male", actual);
		
		
	}
	
	@Test
	public void testCase() throws Exception {
		String text = "CASE "
				+ "WHEN $.code = 'alpha' THEN alpha "
				+ "WHEN $.code = 'beta' THEN beta "
				+ "ELSE gamma "
				+ "END";

		SimpleLocalNameService service = new SimpleLocalNameService();
		service.add(uri("http://example.com/code"));
		service.add(uri("http://example.com/alpha"));
		service.add(uri("http://example.com/beta"));
		service.add(uri("http://example.com/gamma"));
		
		
		FormulaParser parser = new FormulaParser(null, service);

		QuantifiedExpression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		assertTrue(actual.contains("CASE"));
		assertTrue(actual.contains("WHEN $.code = \"alpha\" THEN alpha"));
		assertTrue(actual.contains("WHEN $.code = \"beta\" THEN beta"));
		assertTrue(actual.contains("ELSE gamma"));
		assertTrue(actual.contains("END"));
	}

	@Test
	public void testSubstr() throws Exception {
		String text = "SUBSTR($.name, 3, 5)";
		
		SimpleLocalNameService service = new SimpleLocalNameService();
		FormulaParser parser = new FormulaParser(null, service);
		service.add(Schema.name);
		QuantifiedExpression e = parser.quantifiedExpression(text);
		String actual = e.toSimpleString();
		
		assertEquals(text, actual);
		
	}
	
	
	@Test
	public void testVarId() throws Exception {
		String text = "DAY(?product.dateCreated)";
		
		URI productId = uri("http://example.com/ns/product");
		SimpleLocalNameService service = new SimpleLocalNameService();
		FormulaParser parser = new FormulaParser(null, service);
		service.add(productId);
		service.add(Schema.dateCreated);
		QuantifiedExpression e = parser.quantifiedExpression(text);
		
		Term productTerm = e.getContext().getTerm("product");
		assertTrue(productTerm != null);
		
		assertEquals(productId.stringValue(), productTerm.getExpandedIdValue());
		
	}
	
	@Test
	public void testConcat() throws Exception {
		String text = "\n" +
			"@term givenName: <http://schema.org/givenName>\n" +
			"CONCAT($.givenName, \"foo\")";
		QuantifiedExpression e = parser.quantifiedExpression(text);
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof FunctionExpression);
		FunctionExpression func = (FunctionExpression) primary;
		assertEquals(FunctionExpression.CONCAT, func.getFunctionName());
		assertEquals(2, func.getArgList().size());
		Expression arg1 = func.getArgList().get(0);
		assertTrue(arg1.asPrimaryExpression() instanceof PathExpression);
		assertTrue(func.getArgList().get(1).asPrimaryExpression() instanceof LiteralFormula);
	}
	
	@Test
	public void testNestedPath() throws Exception {
		String text = 
			"@prefix xapi : <http://example.org/xapi/> .\n" +
			"@prefix xid : <http://example.org/xid/> .\n" +
			"@term identifiedBy xid:identifiedBy\n" +
			"@term identityProvider xid:identityProvider\n" +
			"@term identifier xid:identifier\n" +
			"@term name xapi:name\n" +
			"@term homePage xapi:homePage\n" +
			"$.identifiedBy[" + 
			"  identityProvider $.homePage;\n" +
			"  identifier $.name\n" +
			"]";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof PathExpression);
	
		PathExpression path = (PathExpression) primary;
		assertEquals(2, path.getStepList().size());
		
		PathStep step = path.getStepList().get(1);
		assertTrue(step instanceof HasPathStep);
		
		URI identityProvider = uri("http://example.org/xid/identityProvider");
		URI identifier = uri("http://example.org/xid/identifier");
		
		HasPathStep bnode = (HasPathStep) step;
		assertEquals(2, bnode.getConstraints().size());
		PredicateObjectList providerList = bnode.getConstraints().get(0);
		assertEquals(identityProvider, providerList.getVerb().getIri());
		
		PrimaryExpression providerExpression = providerList.getObjectList().getExpressions().get(0).asPrimaryExpression();
		assertTrue(providerExpression instanceof PathExpression);
		
		PathExpression providerPath = (PathExpression) providerExpression;
		assertEquals("$.homePage", providerPath.simpleText());
		
		
		
		PredicateObjectList identifierList = bnode.getConstraints().get(1);
		assertEquals(identifier, identifierList.getVerb().getIri());
		PrimaryExpression identifierExpression = identifierList.getObjectList().getExpressions().get(0).asPrimaryExpression();
		assertTrue(identifierExpression instanceof PathExpression);
		
		PathExpression identifierPath = (PathExpression) identifierExpression;
		assertEquals("$.name", identifierPath.simpleText());
		
		
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	@Test
	public void testDistinct() throws Exception {
		String text = 
			"@term dateCreated <http://schema.org/dateCreated>\n" +
			"COUNT( DISTINCT $.dateCreated )";
		
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
			"UNIX_TIME($.dateCreated)";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof FunctionExpression);
	
		FunctionExpression function = (FunctionExpression) primary;
		assertEquals("UNIX_TIME", function.getFunctionName());
	}
	
	
	@Test
	public void testTerm() throws Exception {
		String text =  "\n"+
			"@term address <http://schema.org/address>\n" +
			"@prefix schema : <http://schema.org/> .\n" +
			"@term postalCode schema:postalCode\n" +
			"$.address.postalCode";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		Context context = e.getContext();
		context.compile();
		
		Term term = context.getTerm("address");
		assertEquals(Schema.address, term.getExpandedId());
		
		term = context.getTerm("postalCode");
		assertEquals(Schema.postalCode, term.getExpandedId());
		
		String actualText = e.toString();
		String expectedText =  "\n"+
				"@prefix schema: <http://schema.org/> .\n" + 
				"@term address <http://schema.org/address>\n" + 
				"@term postalCode schema:postalCode\n" + 
				"\n" + 
				"$.address.postalCode";
		
		assertEquals(expectedText, actualText);
	}
	
	
	
	
	@Test
	public void testCountFormula() throws Exception {

		String text =  "\n"+
			"@term price <http://schema.org/price>\n" + 
			"@term x <http://www.konig.io/ns/var/x>\n\n" + 
			"COUNT(?x.price)";

		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		String expected = text;
		assertEquals(expected, actual);
		
	}
	

	@Test
	public void testOutPathStartingWithE() throws Exception {
		
		String text = "$.estimatedPoints";
		URI estimatedPoints = uri("http://example.com/estimatedPoints");
		Expression e = parser.quantifiedExpression(text, estimatedPoints);
		
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
		String text =  "\n"+
			"@term address <http://schema.org/address>\n" + 
			"@term addressCountry <http://schema.org/addressCountry>\n" + 
			"@term addressRegion <http://schema.org/addressRegion>\n\n" + 
			"$.address[addressCountry \"US\"; addressRegion \"VA\"]";
		
		QuantifiedExpression e = parser.quantifiedExpression(text);
		PrimaryExpression primary = e.asPrimaryExpression();
		assertTrue(primary instanceof PathExpression);
		PathExpression path = (PathExpression) primary;
		List<PathStep> stepList = path.getStepList();
		assertEquals(2, stepList.size());
		PathStep step = stepList.get(1);
		assertTrue(step instanceof HasPathStep);
		HasPathStep hasStep = (HasPathStep) step;
		List<PredicateObjectList> list = hasStep.getConstraints();
		assertEquals(2, list.size());
		
		
		String actual = e.toString();
		assertEquals(text, actual);
	}

	@Test
	public void testBound() throws Exception {
		String text =  "\n"+
			"@term price <http://schema.org/price>\n\n" + 
			"BOUND($.price)" ;
		
		Expression e = parser.quantifiedExpression(text);
		String actual = e.toString();
		String expected = text;
		assertEquals(expected, actual);
		
	}
	
	
//	public void testSum() throws Exception {
//		String text = "\n"+
//			"@term price <http://schema.org/price>\n" + 
//			"@term OfferShape <http://example.com/ns/OfferShape>\n" + 
//			"@term hasShape <http://www.konig.io/ns/core/hasShape>\n" +
//			"@term x <http://www.konig.io/ns/var/x>\n\n" +
//			
//			"SUM(?x.price)\n" + 
//			"WHERE\n" + 
//			"   ?x hasShape OfferShape .\n" + 
//			"";
//		
//		Expression e = parser.quantifiedExpression(text);
//		String actual = e.toString();
//		String expected = text;
//		assertEquals(expected, actual);
//		
//	}
	
	@Test
	public void testIfPlus() throws Exception {
		String text = 
			"IF($.reviewer0Approved, 1, 0)\n" + 
			"+ IF($.reviewer1Approved, 1, 0)\n" + 
			"+ IF($.reviewer2Approved, 1, 0)\n" + 
			"+ IF($.reviewer3Approved, 1, 0)";

		Expression e = parser.expression(text);
		String actual = e.toString();
		
		String expected = "IF($.reviewer0Approved , 1 , 0) + IF($.reviewer1Approved , 1 , 0) + IF($.reviewer2Approved , 1 , 0) + IF($.reviewer3Approved , 1 , 0)";
		assertEquals(expected, actual);
	}
	
	@Test
	public void testIfFunction() throws Exception {
		
		String text =  "\n"+
			"@term email <http://schema.org/email>\n\n" + 
			"IF($.email , 1 , 0)";
		
		Expression e = parser.expression(text);
		String actual = e.toString();
		assertEquals(text, actual);
	}
	
	@Test
	public void testNotEquals() throws Exception {
		String text =  "\n"+
			"@term created <http://www.konig.io/ns/core/created>\n" + 
			"@term modified <http://www.konig.io/ns/core/modified>\n" + 
			"\n" + 
			"$.created != $.modified";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testContext() throws Exception {
		
		String text = "\n"+
			  "@prefix schema: <http://schema.org/> .\n" + 
			  "@term knows schema:knows\n" + 
			  "@term Alice <http://example.com/Alice>\n\n"
			+ "$.knows.knows = Alice";

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

		String text =  "\n"+
				"@term pmd <http://example.com/pmd>\n\n" + 
				"($.sprintIssue.status = pmd:Complete) ? $.sprintIssue.timeEstimate : 0";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testCurieTerm() throws Exception {

		String text =  "\n"+
				"@term ex <http://example.com/core>\n\n" + 
				"$.ex:alpha.ex:beta NOT IN (ex:foo , ex:bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	
	@Test
	public void testIriTerm() throws Exception {

		String text = "$.<http://example.com/alpha>.beta NOT IN (foo , bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}

	@Test
	public void testNotIn() throws Exception {

		String text = "$.alpha.beta NOT IN (foo , bar)";
		
		
		QuantifiedExpression e = parser.quantifiedExpression(text, term("alpha"), term("beta"), term("foo"), term("bar"));
		
		String actual = e.toSimpleString();
	
		assertEquals(text, actual);
	}
	private URI term(String text) {
		return uri("http://example.com/term/" + text);
	}

	@Test
	public void testIn() throws Exception {

		String text = "$.alpha.beta IN (foo , bar)";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals(text, actual);
	}
	
	@Test
	public void testEquals() throws Exception {

		String text = "$.alpha.beta = $.one.two";
		
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

		String text = "$.address.streetAddress + 3";
		
		Expression e = parser.expression(text);
		
		String actual = e.toString();
	
		assertEquals("$.address.streetAddress + 3", actual);
	}
	
	@Test
	public void testBracket() throws Exception {

		String text = "($.owner.age + 3*7)/($.owner.weight*4)";
		
		Expression e = parser.expression(text);
		
		String expected = "($.owner.age + 3 * 7) / ($.owner.weight * 4)";
		String actual = e.toString();
	
		assertEquals(expected, actual);
	}

}
