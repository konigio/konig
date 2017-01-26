package io.konig.rio.turtle;

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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;


public class TurtleParserTest {

	private ValueFactory vf = ValueFactoryImpl.getInstance();
	
	@Test
	public void testIriIriIriStatement() throws Exception {
		URI aliceId = uri("http://example.com/person/Alice");
		URI personId = uri("http://schema.org/Person");
		
		Statement st = vf.createStatement(aliceId, RDF.TYPE, personId);
		
		
		String text =
				  "<http://example.com/person/Alice>"
				+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"
				+ " <http://schema.org/Person> .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		Model model = handler.model;
		
		assertEquals(1, model.size());		
		
		assertTrue(handler.model.contains(st));
	}
	
	@Test
	public void testLittleA() throws Exception {
		URI aliceId = uri("http://example.com/person/Alice");
		URI personId = uri("http://schema.org/Person");
		
		Statement st = vf.createStatement(aliceId, RDF.TYPE, personId);
		
		
		String text =
				  "<http://example.com/person/\u0041li\u0063e>"
				+ " a"
				+ " <http://schema.org/Person> .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		Model model = handler.model;
		
		assertEquals(1, model.size());		
		
		assertTrue(handler.model.contains(st));
	}
	
	@Test
	public void testUchar() throws Exception {

		URI aliceId = uri("http://example.com/person/Alice");
		URI personId = uri("http://schema.org/Person");
		
		Statement st = vf.createStatement(aliceId, RDF.TYPE, personId);
		
		
		String text =
				  "<http://example.com/person/\u0041li\u0063e>"
				+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"
				+ " <http://schema.org/Person> .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		Model model = handler.model;
		
		assertEquals(1, model.size());		
		
		assertTrue(handler.model.contains(st));
	}
	
	@Test
	public void testObjectList() throws Exception {

		URI aliceId = uri("http://example.com/person/Alice");
		URI personId = uri("http://schema.org/Person");
		URI Mother = uri("http://example.com/ns/Mother");
		
		Statement aliceIsAPerson = statement(aliceId, RDF.TYPE, personId);
		Statement aliceIsAMother = statement(aliceId, RDF.TYPE, Mother);
		
		
		String text =
				  "<http://example.com/person/\u0041li\u0063e> \n"
				+ " a\n"
				+ " <http://schema.org/Person> ,\n"
				+ " <http://example.com/ns/Mother> .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		Model model = handler.model;
		
		assertEquals(2, model.size());		
		
		assertTrue(model.contains(aliceIsAPerson));
		assertTrue(model.contains(aliceIsAMother));
	}
	
	@Test
	public void testPredicateObjectList() throws Exception {

		URI aliceId = uri("http://example.com/person/Alice");
		URI personId = uri("http://schema.org/Person");
		URI parent = uri("http://schema.org/parent");
		URI sallyId = uri("http://example.com/person/Sally");
		
		Statement aliceIsAPerson = statement(aliceId, RDF.TYPE, personId);
		Statement aliceParentSally = statement(aliceId, parent, sallyId);
		
		
		String text =
				  "<http://example.com/person/\u0041li\u0063e> \n"
				+ " a <http://schema.org/Person> ;\n"
				+ " <http://schema.org/parent> <http://example.com/person/Sally> .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		Model model = handler.model;
		
		assertEquals(2, model.size());		
		
		assertTrue(model.contains(aliceIsAPerson));
		assertTrue(model.contains(aliceParentSally));
	}
	
	@Test
	public void testPrefixID() throws Exception {
		
		String text = 
			  "@prefix schema: <http://schema.org/>.\n"
			+ "@prefix ex : <http://example.com/ns/> .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		assertEquals("http://schema.org/", handler.getNamespaceURI("schema"));
		assertEquals("http://example.com/ns/", handler.getNamespaceURI("ex"));
		
		
	}
	
	@Test
	public void testPrefixedName() throws Exception {
		
		String text =
			  "@prefix  schema: <http://schema.org/>.\n"
			+ "@prefix person: <http://example.com/person/>."
			+ "person:alice schema:parent person:sally .";
		
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		Model model = handler.model;
		
		assertTrue(model.contains(
			uri("http://example.com/person/alice"), 
			uri("http://schema.org/parent"), 
			uri("http://example.com/person/sally")
		));
	}

	
	@Test
	public void testBNodeLabel() throws Exception {
		String text =
				  "@prefix  schema: <http://schema.org/>.\n"
				+ "_:alice schema:parent _:sally .";
			
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			Model model = handler.model;
			
			assertTrue(model.contains(
				bnode("alice"), 
				uri("http://schema.org/parent"), 
				bnode("sally")
			));
	}
	
	@Test
	public void testBNodePropertyListSubject1() throws Exception {

		String text =
			  "@prefix  schema: <http://schema.org/>.\n"
			+ "@prefix person: <http://example.com/person/>."
			+ "["
			+ "  schema:parent person:sally, person:bob ;"
			+ "  schema:sibling person:carl"
			+ "] .";
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI sibling = uri("http://schema.org/sibling");
		URI parent = uri("http://schema.org/parent");
		URI sally = uri("http://example.com/person/sally");
		URI bob = uri("http://example.com/person/bob");
		URI carl = uri("http://example.com/person/carl");
		Model model = handler.model;
		
		assertEquals(model.size(), 3);
		
		Resource subject = model.iterator().next().getSubject();
		assertTrue(subject instanceof BNode);
		
		assertTrue(model.contains(subject, parent, sally));
		assertTrue(model.contains(subject, parent, bob));
		assertTrue(model.contains(subject, sibling, carl));
	}
	
	@Test
	public void testBNodePropertyListSubject2() throws Exception {

		String text =
			  "@prefix  schema: <http://schema.org/>.\n"
			+ "@prefix person: <http://example.com/person/>."
			+ "["
			+ "  schema:sibling person:carl"
			+ "] schema:parent person:sally, person:bob .";
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI sibling = uri("http://schema.org/sibling");
		URI parent = uri("http://schema.org/parent");
		URI sally = uri("http://example.com/person/sally");
		URI bob = uri("http://example.com/person/bob");
		URI carl = uri("http://example.com/person/carl");
		Model model = handler.model;
		
		assertEquals(model.size(), 3);
		
		Resource subject = model.iterator().next().getSubject();
		assertTrue(subject instanceof BNode);
		
		assertTrue(model.contains(subject, parent, sally));
		assertTrue(model.contains(subject, parent, bob));
		assertTrue(model.contains(subject, sibling, carl));
	}
	
	@Test
	public void testBNodePropertyListObject() throws Exception {

		String text =
			  "@prefix  schema: <http://schema.org/>."
			+ "@prefix person: <http://example.com/person/>."
			+ "person:alice schema:parent ["
			+ "  schema:spouse person:bob"
			+ "] , person:bob ;"
			+ "  schema:sibling person:carl .";
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI sibling = uri("http://schema.org/sibling");
		URI parent = uri("http://schema.org/parent");
		URI spouse = uri("http://schema.org/spouse");
	
		URI bob = uri("http://example.com/person/bob");
		URI carl = uri("http://example.com/person/carl");
		URI alice = uri("http://example.com/person/alice");
		Model model = handler.model;
		
		Model filteredModel = model.filter(null, spouse, bob);
		
		assertEquals(1, filteredModel.size());
		
		Resource bnode = filteredModel.iterator().next().getSubject();
		assertTrue(bnode instanceof BNode);
		
		
		assertTrue(model.contains(alice, parent, bnode));
		assertTrue(model.contains(alice, parent, bob));
		assertTrue(model.contains(bnode, spouse, bob));
		assertTrue(model.contains(alice, sibling, carl));
	}
	
	@Test
	public void testAnon() throws Exception {

		String text =
			  "@prefix  schema: <http://schema.org/>.\n"
			+ "@prefix person: <http://example.com/person/>.\n"
			+ "[]\n"
			+ " schema:sibling person:carl ;\n"
			+ " schema:parent person:sally, person:bob .\n";
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI sibling = uri("http://schema.org/sibling");
		URI parent = uri("http://schema.org/parent");
		URI sally = uri("http://example.com/person/sally");
		URI bob = uri("http://example.com/person/bob");
		URI carl = uri("http://example.com/person/carl");
		Model model = handler.model;
		
		assertEquals(model.size(), 3);
		
		Resource subject = model.iterator().next().getSubject();
		assertTrue(subject instanceof BNode);
		
		assertTrue(model.contains(subject, parent, sally));
		assertTrue(model.contains(subject, parent, bob));
		assertTrue(model.contains(subject, sibling, carl));
	}

	@Test
	public void testBoolean() throws Exception {

		String text =
			  "@prefix  ex: <http://example.com/ns/>.\n"
			+ "@prefix trueLove: <http://truelove.com/> .\n"
			+ "@prefix person: <http://example.com/person/>.\n"
			+ "person:alice \n"
			+ "  ex:isHappy true ;\n"
			+ "  ex:hatesWork false ;\n"
			+ "  ex:admires trueLove:bob .";
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI isHappy = uri("http://example.com/ns/isHappy");
		URI hatesWork = uri("http://example.com/ns/hatesWork");
		URI admires = uri("http://example.com/ns/admires");
		URI alice = uri("http://example.com/person/alice");
		URI bob = uri("http://truelove.com/bob");
		Literal TRUE = BooleanLiteralImpl.TRUE;
		Literal FALSE = BooleanLiteralImpl.FALSE;
		
		Model model = handler.model;
		
		assertEquals(model.size(), 3);
		
		
		assertTrue(model.contains(alice, isHappy, TRUE));
		assertTrue(model.contains(alice, hatesWork, FALSE));
		assertTrue(model.contains(alice, admires, bob));
	}
	
	@Test
	public void testUnsignedInteger() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 123 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("123", XMLSchema.INTEGER);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}
	
	@Test
	public void testPositiveInteger() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> +123 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("+123", XMLSchema.INTEGER);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	@Test
	public void testNegativeInteger() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> -123 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("-123", XMLSchema.INTEGER);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}
	
	@Test
	public void testUnsignedDecimal() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 123.456 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("123.456", XMLSchema.DECIMAL);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}
	
	@Test
	public void testPositiveDecimal() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> +123.456 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("+123.456", XMLSchema.DECIMAL);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	
	@Test
	public void testNegativeDecimal() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> -123.456 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("-123.456", XMLSchema.DECIMAL);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	@Test
	public void testDoubleWithIntegerDecimalAndUnsignedExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 0.4e10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("0.4e10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	@Test
	public void testDoubleWithIntegerDecimalAndPositiveExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 0.4e+10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("0.4e+10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	@Test
	public void testDoubleWithIntegerDecimalAndNegativeExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 0.4e-10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("0.4e-10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	@Test
	public void testDoubleWithDecimalAndPositiveExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> .4e+10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal(".4e+10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}
	

	@Test
	public void testDoubleWithDecimalAndNegativeExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> .4e-10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal(".4e-10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	@Test
	public void testDoubleWithIntegerAndNegativeExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 4e-10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("4e-10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}
	
	@Test
	public void testDoubleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"hello world\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("hello world", literal.stringValue());
	}
	
	@Test
	public void testSingeQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> 'hello world' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("hello world", literal.stringValue());
	}
	
	@Test
	public void testLongDoubleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\"\"hello world\"\"\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("hello world", literal.stringValue());
	}

	
	@Test
	public void testLongSingleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '''hello world''' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("hello world", literal.stringValue());
	}
	
	@Test
	public void testLongDoubleQuoteECHAR() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\"\"\\t\\b\\n\\r\\f\\\"\\'\\\\\"\"\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\t\b\n\r\f\"'\\", literal.stringValue());
	}
	
	@Test
	public void testLongSingleQuoteECHAR() throws Exception {
		String text =
			"<http://example.com/foo> <http://example.com/bar> '''\\t\\b\\n\\r\\f\\\"\\'\\\\''' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\t\b\n\r\f\"'\\", literal.stringValue());
	}
	

	@Test
	public void testLongDoubleQuoteWithInternalQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\"\"Hello \"Alice\", my friend\"\"\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("Hello \"Alice\", my friend", literal.stringValue());
	}

	@Test
	public void testLongDoubleQuoteWithNewLines() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\"\"Hello Alice.\nHow are you?\"\"\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("Hello Alice.\nHow are you?", literal.stringValue());
	}


	@Test
	public void testLongDoubleQuoteWithCarriageReturnNewLine() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\"\"Hello Alice.\r\nHow are you?\"\"\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("Hello Alice.\r\nHow are you?", literal.stringValue());
	}
	
	

	
	@Test
	public void testSingleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> 'hello world' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("hello world", literal.stringValue());
	}
	

	
	@Test
	public void testTypedLiteral() throws Exception {
		String text =
				  "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>."
				+ "<http://example.com/foo> <http://example.com/bar> '1.2'^^xsd:float ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("1.2", literal.stringValue());
			assertEquals(XMLSchema.FLOAT, literal.getDatatype());
	}

	@Test
	public void testLangString() throws Exception {
		String text =
				  "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>."
				+ "<http://example.com/foo> <http://example.com/bar> 'nice'@en-us ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("nice", literal.stringValue());
			assertEquals("en-us", literal.getLanguage());
	}
	
	@Test
	public void testDoubleQuoteTab() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\t\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\t", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteTab() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\t' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\t", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteBackspace() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\b\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\b", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteNewLine() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\n\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\n", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteNewLine() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\n' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\n", literal.stringValue());
	}

	
	@Test
	public void testDoubleQuoteCarriageReturn() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\r\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\r", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteCarriageReturn() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\r' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\r", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteFormFeed() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\f\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\f", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteFormFeed() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\f' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\f", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteDoubleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\\"\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\"", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteEscapeDoubleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\\"' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\"", literal.stringValue());
	}

	
	@Test
	public void testSingleQuoteBareDoubleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\"' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\"", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteSingleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"'\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("'", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteSingleQuote() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\'' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("'", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteBackslash() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\\\\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\\", literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteBackslash() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\\\\' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			assertEquals("\\", literal.stringValue());
	}
	
	@Test
	public void testDoubleQuoteUCHAR() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> \"\\u0041\\U00010437\" ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			StringBuilder builder = new StringBuilder();
			builder.append('A');
			builder.appendCodePoint(Integer.parseInt("10437", 16));
			String expected = builder.toString();
			assertEquals(expected, literal.stringValue());
	}
	
	@Test
	public void testSingleQuoteUCHAR() throws Exception {
		String text =
				"<http://example.com/foo> <http://example.com/bar> '\\u0041\\U00010437' ." ;
				
			StringReader reader = new StringReader(text);
			
			TurtleParser parser = new TurtleParser();
			Handler handler = new Handler();
			parser.setRDFHandler(handler);
			
			parser.parse(reader, "");
			
			Model model = handler.model;
			Statement st = model.iterator().next();
			Value value = st.getObject();
			assertTrue(value instanceof Literal);
			Literal literal = (Literal) value;
			StringBuilder builder = new StringBuilder();
			builder.append('A');
			builder.appendCodePoint(Integer.parseInt("10437", 16));
			String expected = builder.toString();
			assertEquals(expected, literal.stringValue());
	}
	

	@Test
	public void testDoubleWithIntegerDotExponent() throws Exception {

		String text =
			"<http://example.com/ball> <http://example.com/speed> 4.e-10 ." ;
			
		StringReader reader = new StringReader(text);
		
		TurtleParser parser = new TurtleParser();
		Handler handler = new Handler();
		parser.setRDFHandler(handler);
		
		parser.parse(reader, "");
		
		URI subject = uri("http://example.com/ball");
		URI predicate = uri("http://example.com/speed");
		Literal value = literal("4.e-10", XMLSchema.DOUBLE);
		
		Model model = handler.model;
		assertTrue(model.contains(subject, predicate, value));
	}

	private Literal literal(String text, URI datatype) {
		return vf.createLiteral(text, datatype);
	}

	private BNode bnode(String label) {
		return new BNodeImpl(label);
	}
	
	private URI uri(String text) {
		return new URIImpl(text);
	}
	
	private Statement statement(Resource subject, URI predicate, Value object) {
		return vf.createStatement(subject, predicate, object);
	}
	
	static class Handler extends RDFHandlerBase {
		
		private Map<String,String> namespaceMap = new HashMap<>();
		
		Model model = new LinkedHashModel();
		
		public String getNamespaceURI(String prefix) {
			return namespaceMap.get(prefix);
		}
		
		@Override
		public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
			namespaceMap.put(prefix, uri);
		}

		@Override
		public void handleStatement(Statement st)
			throws RDFHandlerException
		{
			model.add(st);
//			System.out.println(st);
		}
	}

}
