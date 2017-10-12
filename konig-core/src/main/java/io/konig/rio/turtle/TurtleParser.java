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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;

import io.konig.core.util.IriTemplate;

public class TurtleParser extends RDFParserBase {

	
	protected PushbackReader reader;
	protected StringBuilder buffer = new StringBuilder();
	private String baseURI;
	private int lineNumber=1;
	private int columnNumber;
	protected NamespaceMap namespaceMap;
	
	public TurtleParser() {
		namespaceMap = new HashNamespaceMap();
	}
	
	public TurtleParser(NamespaceMap namespaceMap) {
		this.namespaceMap = namespaceMap==null ? new HashNamespaceMap() : namespaceMap;
	}
	
	public TurtleParser(NamespaceMap namespaceMap, ValueFactory valueFactory) {
		super(valueFactory);
		this.namespaceMap = namespaceMap;
	}
	
	protected StringBuilder buffer() {
		buffer.setLength(0);
		return buffer;
	}
	
	public NamespaceMap getNamespaceMap() {
		return namespaceMap;
	}

	private void turtleDoc() throws IOException, RDFParseException, RDFHandlerException {
		lineNumber = 1;
		columnNumber = 0;
		if (rdfHandler != null) {
			rdfHandler.startRDF();
		}
		int c = 0;
		while (c != -1) {
			statement();
			c = next();
			unread(c);
		}
		
		if (rdfHandler!= null) {
			rdfHandler.endRDF();
		}
		
	}
	
	private void statement() throws IOException, RDFParseException, RDFHandlerException {
		int c = next();
		
		if (c == '@') {
			directive(c);
		} else {
			triples(c);
			read('.');
		}
		
	}

	protected void directive(int c) throws IOException, RDFParseException, RDFHandlerException {
		
		
		if (tryWord("prefix")) {
			prefixID();
			
		} else if (tryWord("base")) {
			base();
		}
		
		
	}


	protected void base() throws RDFParseException {
		fail("@base directive is not supported");
		// TODO: implement @base directive
		
	}

	/**
	 * <pre>
	 * prefixID	::=	'@prefix' PNAME_NS IRIREF '.'
	 * </pre>
	 * This method assumes we have already read '@prefix'
	 */
	protected void prefixID() throws RDFParseException, IOException, RDFHandlerException {

		readSpace();
		String prefix = pname_ns();
		String iriRef = iriRef(next());
		read('.');
		
		namespaceMap.put(prefix, iriRef);
		namespace(prefix, iriRef);
	}
	
	protected void namespace(String prefix, String name) throws RDFHandlerException {

		if (rdfHandler!=null) {
			rdfHandler.handleNamespace(prefix, name);
		}
	}

	/**
	 * <pre>
	 * PNAME_NS	::=	PN_PREFIX? ':'
	 * </pre>
	 */
	protected String pname_ns() throws IOException, RDFParseException {
		StringBuilder builder = buffer();

		int c = next();
		pn_prefix(c);
		
		c = next();
		if (c != ':') {
			builder = err();
			builder.append("Expected ':' but found '");
			appendCodePoint(builder, c);
			builder.append("'");
			fail(builder);
		}
		
		
		return builder.toString();
	}
	
	protected String pn_prefix() throws RDFParseException, IOException {
		buffer();
		int c = next();
		pn_prefix(c);
		return buffer.toString();
	}

	/**
	 * PN_PREFIX	::=	PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
	 */
	protected void pn_prefix(int c) throws IOException, RDFParseException {
		if (pn_chars_base(c)) {
			buffer.appendCodePoint(c);
			
			boolean endsWithDot=false;
			for (;;) {
				c = read();
				if (pn_chars(c)) {
					buffer.appendCodePoint(c);
					endsWithDot = false;
				} else if (c=='.') {
					buffer.appendCodePoint(c);
					endsWithDot = true;
				} else {
					break;
				}
			}
			if (endsWithDot) {
				throw new  RDFParseException("Namespace prefix cannot end with '.'");
			}
			
		} 
		unread(c);
		
	}


	/**
	 * PN_CHARS	::=	PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
	 */
	protected boolean pn_chars(int c) {
		return 
			pn_chars_u(c) ||
			(c == '-') ||
			inRange(c, '0', '9') ||
			(c == 0xB7) ||
			inRange(c, 0x300, 0x36F) ||
			inRange(c, 0x203F, 0x2040);
	}

	/**
	 * PN_CHARS_U	::=	PN_CHARS_BASE | '_'
	 */
	protected boolean pn_chars_u(int c) {
		return 
			pn_chars_base(c) ||
			(c == '_') ;
	}

	protected boolean pn_chars_base(int c)  {
		
		
		return 
			inRange(c, 'A', 'Z') ||
			inRange(c, 'a', 'z') ||
			inRange(c, 0xC0, 0xD6) ||
			inRange(c, 0xD8, 0xF6) ||
			inRange(c, 0xF8, 0x2FF) ||
			inRange(c, 0x370, 0x37D) ||
			inRange(c, 0x37F, 0x1FFF) ||
			inRange(c, 0x200C, 0x200D) ||
			inRange(c, 0x2070, 0x218F) ||
			inRange(c, 0x2C00, 0x2FEF) ||
			inRange(c, 0x3001, 0xD7FF) ||
			inRange(c, 0xF900, 0xFDCF) ||
			inRange(c, 0xFDF0, 0xFFFD) ||
			inRange(c, 0x10000, 0xEFFFF) 
		;
	}

	protected boolean inRange(int c, int min, int max) {
		
		return c>=min && c<=max;
	}

	protected void readSpace() throws IOException, RDFParseException {
		
		int c = read();
		if (!isWhitespace(c)) {
			StringBuilder err = err();
			err.append("Expected whitespace but found '");
			appendCodePoint(err, c);
			err.append("'");
			fail(err);
		}
		skipSpace();
		
	}
	
	protected boolean skipSpace() throws IOException {
		boolean result = false;
		int c = read();
		while (isWhitespace(c)) {
			result = true;
			c = read();
		}
		unread(c);
		
		return result;
		
	}

	protected void triples(int c) throws RDFParseException, IOException, RDFHandlerException {
		boolean done = false;
		if (c == '[') {
			BNode subject = tryBlankNodePropertyList(c);
			if (subject != null) {
				done = true;
				c = next();
				unread(c);
				if (c != '.') {
					predicateObjectList(subject);
				}
			}
		} 
		if (!done) {
			Resource subject = subject(c);
			predicateObjectList(subject);
		}
		
	}

	protected void predicateObjectList(Resource subject) throws IOException, RDFParseException, RDFHandlerException {
		URI predicate = verb();
		objectList(subject, predicate);
		
		int c = next();
		
		while (c == ';') {
			predicate = verb();
			objectList(subject, predicate);
			c = next();
		}

		unread(c);
		
	}

	private void objectList(Resource subject, URI predicate) throws IOException, RDFHandlerException, RDFParseException {
		int c = next();
		
		Value object = object(c);
		statement(subject, predicate, object);
		
		c = next();
		while (c == ',') {
			c = next();
			object = object(c);
			statement(subject, predicate, object);
			c = next();
		}
		unread(c);
		
		
	}

	protected void statement(Resource subject, URI predicate, Value object) throws RDFHandlerException {
		
		if (rdfHandler != null) {
			Statement st = valueFactory.createStatement(subject, predicate, object);
			rdfHandler.handleStatement(st);
		}
		
	}

	/**
	 * <pre>
	 * object	::=	iri | BlankNode | collection | blankNodePropertyList | literal
	 * </pre>
	 */
	protected Value object(int c) throws RDFParseException, IOException, RDFHandlerException {
		
		Value value = null;
		value =
			(value=tryCollection(c)) != null ? value :
			(value=tryLiteral(c)) != null ? value :
			(value=tryBlankNode(c)) != null ? value :
			(value=tryBlankNodePropertyList(c)) != null ? value :
			(value=tryIri(c)) != null ? value :
			null;
		
		if (value == null) {
			fail("Expected (iri | BlankNode | collection | blankNodePropertyList | literal)");
		}
	
		return value;
	}

	private Value tryCollection(int c) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected BNode blankNodePropertyList() throws IOException, RDFParseException, RDFHandlerException {
		int c = read();
		BNode result = tryBlankNodePropertyList(c);
		if (result == null) {
			unread(c);
		}
		return result;
	}

	/**
	 * <pre>
	 * 	blankNodePropertyList	::=	'[' predicateObjectList ']'
	 * </pre>
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 */
	protected BNode tryBlankNodePropertyList(int c) throws IOException, RDFParseException, RDFHandlerException {
		if (c != '[') {
			return null;
		}

		c = next();
		unread(c);
		if (c == ']') {
			// No properties inside this BNode.
			
			return null;
		}
		
		BNode bnode = valueFactory.createBNode();
		predicateObjectList(bnode);
		
		c = next();
		if (c != ']') {
			StringBuilder builder = err();
			builder.append("Expected ']' but found '");
			appendCodePoint(builder, c);
			builder.append("'");
			fail(builder);
		}
		
		return bnode;
	}

	protected void appendCodePoint(StringBuilder builder, int c) {

		if (c == -1) {
			builder.append("<<EOF>>");
		} else {
			builder.appendCodePoint(c);
		}
		
	}
	
	protected Literal tryLiteral() throws IOException, RDFParseException, RDFHandlerException {
		int c = read();
		Literal literal = tryLiteral(c);
		if (literal == null) {
			unread(c);
		}
		return literal;
	}

	/**
	 * <pre>
	 * literal	::=	RDFLiteral | NumericLiteral | BooleanLiteral
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 * @throws RDFHandlerException 
	 */
	private Literal tryLiteral(int c) throws IOException, RDFParseException, RDFHandlerException {
		
		Literal value = null;
		value =
			(value=tryRDFLiteral(c)) != null ? value :
			(value=tryNumericLiteral(c)) != null ? value :
			(value=tryBooleanLiteral(c)) != null ? value :
			null;
		return value;
	}

	private Literal tryBooleanLiteral(int c) throws IOException {
	
		Literal result = null;
		if  ((c=='t') && tryWord("rue")) {
			result = valueFactory.createLiteral(true);
		} else if ((c=='f') && tryWord("alse")) {
			result = valueFactory.createLiteral(false);
		}
		if (result != null) {
			c = read();
			unread(c);
			if (!isWhitespace(c) && (c != ',') && (c!=';') && (c!='.') && (c!='/') && (c!='^') && (c!=']')) {
				boolean value = result.booleanValue();
				if (value) {
					unread("rue");
				} else {
					unread("alse");
				}
				result = null;
			}
			
		}
		return result;
	}

	protected void unread(String text) throws IOException {
		for (int i=text.length()-1; i>=0; i--) {
			unread(text.charAt(i));
		}
		
	}

	/**
	 * <pre>
	 * NumericLiteral ::= INTEGER | DECIMAL | DOUBLE
	 *                
	 * INTEGER ::= [+-]? [0-9]+
	 * 
	 * DECIMAL ::= [+-]? [0-9]* '.' [0-9]+
	 * 
	 * DOUBLE ::= [+-]? ([0-9]+ '.' [0-9]* EXPONENT | '.' [0-9]+ EXPONENT | [0-9]+ EXPONENT)
	 *      
	 *        
	 * </pre> 
	 * 
	 * We have the following possible parses
	 * <pre>
	 * 
	 * 	[+-]? 
	 *     [0-9]+                      >>>> INTEGER
	 *        '.' [0-9]+               >>>> DECIMAL
	 *        '.' [0-9]+ EXPONENT      >>>> DOUBLE
	 *        '.' EXPONENT             >>>> DOUBLE
	 *        EXPONENT                 >>>> DOUBLE
	 *     '.' [0-9]+                  >>>> DECIMAL
	 *     '.' [0-9]+ EXPONENT         >>>> DOUBLE
	 *      
	 * </pre>
	 * 
	 * Let's define the following production rules.
	 * 
	 * <pre>
	 * 
	 *  NumericLiteral ::= UnsignedNumber | [+-] UnsignedNumber
	 *  
	 *  UnsignedNumber ::= NumberWithIntegerPart | DecimalPart ;
	 *  
	 *  NumberWithIntegerPart ::= [0-9]+ (DecimalPart | EXPONENT)?
	 *  
	 *  DecimalPart ::= '.' [0-9]+ EXPONENT?
	 * 
	 * </pre>
	 */
	private Literal tryNumericLiteral(int c) throws IOException, RDFParseException {

		Literal result = null;
		
		boolean digit = isDigit(c);
		
		if (
			!digit && 
			c!='+' &&
			c!='-' &&
			c!= '.'
		) {
			return null;
		}
		
		StringBuilder builder = buffer();
		
		
		if (c=='+' || c=='-') {
			builder.appendCodePoint(c);
			c = read();
			digit = isDigit(c);
		}
		
		if (digit) {
			result = numberWithIntegerPart(c);
		} else if (c=='.') {
			result = tryDecimalPart(c);
			
		} else {
			builder = err();
			builder.append("Invalid numeric literal. Expected [0-9] or '.' but found '");
			builder.appendCodePoint(c);
			builder.append("'");
			fail(builder);
		}
		
		return result;
	}

	/**
	 * <pre>
	 * DecimalPart ::= '.' [0-9]+ EXPONENT?
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private Literal tryDecimalPart(int c) throws IOException, RDFParseException {
		Literal result = null;
		if (c=='.') {
			c = read();
			if (c=='e' | c=='E') {
				buffer.append('.');
				result = tryExponent(c);
			} else if (!isDigit(c)) {
				// Do nothing
			} else {
				buffer.append('.');
				while (isDigit(c)) {
					buffer.appendCodePoint(c);
					c = read();
				}
				
				result = tryExponent(c);
				if (result == null) {
					unread(c);
					result = valueFactory.createLiteral(buffer.toString(), XMLSchema.DECIMAL);
				}
			}
			if (result == null) {
				unread(c);
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * NumberWithIntegerPart ::= [0-9]+ (DecimalPart | EXPONENT)?
	 * </pre>
	 * @return
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private Literal numberWithIntegerPart(int c) throws RDFParseException, IOException {
		
		while (isDigit(c)) {
			buffer.appendCodePoint(c);
			c = read();
		}
		
		Literal result = 
			(result=tryDecimalPart(c)) != null	? result :
			(result=tryExponent(c)) != null 	? result :
			integerLiteral(c);
		
		return result;
	}

	private Literal integerLiteral(int c) throws IOException {
		unread(c);
		return valueFactory.createLiteral(buffer.toString(), XMLSchema.INTEGER);
	}

	/**
	 * <pre>
	 * EXPONENT ::= [eE] [+-]? [0-9]+
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private Literal tryExponent(int c) throws IOException, RDFParseException {
		Literal result = null;
		
		if (c == 'e' || c=='E') {
			buffer.appendCodePoint(c);
			c = read();
			if (c=='+' || c=='-') {
				buffer.appendCodePoint(c);
				c = read();
			}
			if (!isDigit(c)) {
				unread(c);
				return null;
			} 
			while (isDigit(c)) {
				buffer.appendCodePoint(c);
				c = read();
			}
			unread(c);
			result = valueFactory.createLiteral(buffer.toString(), XMLSchema.DOUBLE);
			
		}
		
		return result;
	}
	
	protected boolean isLetter(int c) {
		return inRange(c, 'a', 'z') || inRange(c, 'A', 'Z');
	}

	protected boolean isDigit(int c) {
		
		return c>='0' && c<='9';
	}


	/**
	 * <pre>
	 * 	RDFLiteral	::=	String (LANGTAG | '^^' iri)?
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 * @throws RDFHandlerException 
	 */
	private Literal tryRDFLiteral(int c) throws RDFParseException, IOException, RDFHandlerException {
		
		Literal result = null;
		if (c=='\'' || c=='"') {
			return rdfLiteral(c);
		}
		
		return result;
	}
	
	/**
	 * <pre>
	 * 	RDFLiteral	::=	String (LANGTAG | '^^' iri)?
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 * @throws RDFHandlerException 
	 */
	private Literal rdfLiteral(int c) throws IOException, RDFParseException, RDFHandlerException {
		Literal result = null;
		string(c, buffer());
		c = read();
		
		String text = buffer.toString();
		
		if (c == '@') {
			String lang = langtag(c);
			result = valueFactory.createLiteral(text, lang);
		} else if (c=='^') {
			c = read();
			if (c != '^') {
				err();
				buffer.append("Invalid string literal. Expected '^^' but found '^");
				buffer.appendCodePoint(c);
				buffer.append("'");
				fail(buffer);
			}
			c = read();
			result = valueFactory.createLiteral(text, iri(c));
		} else {
			unread(c);
			result = valueFactory.createLiteral(text, XMLSchema.STRING);
		}
		
		return result;
	}


	/**
	 * <pre>
	 * LANGTAG	::=	'@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
	 * </pre>
	 * @throws IOException 
	 */
	private String langtag(int c) throws RDFParseException, IOException {
		if (c!='@') {
			err();
			buffer.append("Expected '@' but found '");
			buffer.appendCodePoint(c);
			buffer.append("'");
			fail(buffer);
		}
		buffer();
		
		c = read();
		if (!inRange(c, 'a', 'z') && !inRange(c, 'A', 'Z')) {
			err();
			buffer.append("Language tag must start with a letter, but found '");
			buffer.appendCodePoint(c);
			buffer.append("'");
			fail(buffer);
		}
		while (
			inRange(c, 'a', 'z') || 
			inRange(c, 'A', 'Z') ||
			isDigit(c) ||
			c=='-'
		) {
			buffer.appendCodePoint(c);
			c = read();
		}
		unread(c);
		
		return buffer.toString();
	}

	/**
	 * <pre>
	 * String	::=	STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE | STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE
	 * </pre>
	 */
	private void string(int c, StringBuilder builder) throws RDFParseException, IOException {
		
		if (c!='\'' && c!='"') {
			err();
			buffer.append("Expected ['] or [\"] but found [");
			buffer.appendCodePoint(c);
			buffer.append("'");
			fail(buffer);
		}

		int d = read();
		int e = read();
		
		if (c=='\'') {
			if (d=='\'' && e=='\'') {
				string_literal_long_single_quote(c, d, e, builder);
			} else {
				unread(e);
				unread(d);
				string_literal_single_quote(c, builder);
			}
			
		} else {
			if (d=='"' && e=='"') {
				string_literal_long_quote(c, d, e, builder);
			} else {
				unread(e);
				unread(d);
				string_literal_quote(c, builder);
			}
		}
		
		
	}


	/**
	 * <pre>
	 * STRING_LITERAL_QUOTE	::=	'"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'
	 * </pre>
	 * 
	 * where 
	 * <pre>
	 *   #x22=" 
	 *   #x5C=\ 
	 *   #xA=new line 
	 *   #xD=carriage return
	 * </pre>
	 */
	private void string_literal_quote(int c, StringBuilder builder) throws RDFParseException, IOException {
		assertEquals('"', c);
		
		while ( stringQuoteChar(c=read(), builder));
		assertEquals('"', c);
	}	

	protected void assertEqualsIgnoreCase(int expected, int actual) throws RDFParseException {
		if (Character.toUpperCase(expected) != Character.toUpperCase(actual)) {
			StringBuilder err = err();
			int lower = Character.toLowerCase(expected);
			int upper = Character.toUpperCase(expected);
			
			err.append("Expected '");
			err.appendCodePoint(lower);
			err.append("' or '");
			err.appendCodePoint(upper);
			err.append("' but found '");
			appendCodePoint(err, actual);
			err.append("'");
			fail(err);
			
		}
	}
	
	protected void assertIgnoreCase(String expected) throws IOException, RDFParseException {
		for (int i=0; i<expected.length();) {
			int c = expected.codePointAt(i);
			i += Character.charCount(c);
			int d = read();
			assertEqualsIgnoreCase(c, d);
		}
	}
	
	protected void assertEquals(int expected, int actual) throws RDFParseException {
		
		if (actual != expected) {
			err();
			if (actual=='\'') {
				buffer.append("Expected [");
				buffer.appendCodePoint(expected);
				buffer.append("] but found [");
				if (actual == -1) {
					buffer.append("EOF");
				} else {
					buffer.appendCodePoint(actual);
				}
				buffer.append("]");
			} else {
				buffer.append("Expected '");
				buffer.appendCodePoint(expected);
				buffer.append("' but found '");
				if (actual == -1) {
					buffer.append("EOF");
				} else {
					buffer.appendCodePoint(actual);
				}
				buffer.append("'");
			}
			fail(buffer);
		}
		
		
	}
	
	protected void assertNext(int expected) throws IOException, RDFParseException {
		int actual = next();
		assertEquals(expected, actual);
	}
	
	protected void assertWhitespace() throws IOException, RDFParseException {
		int c = read();
		if (!isWhitespace(c)) {
			fail("Expected whitespace");
		}
		skipSpace();
	}

	private boolean stringQuoteChar(int c, StringBuilder builder) throws IOException, RDFParseException {
		
		if (echar(c, builder) || uchar(c, builder)) {
			return true;
		}
		if (c!='"' && c!='\\' && c!='\n' && c!='\r') {
			builder.appendCodePoint(c);
			return true;
		}
		
		return false;
	}

	/**
	 * ECHAR	::=	'\' [tbnrf"'\]
	 * @param c
	 * @param builder
	 * @return
	 * @throws IOException 
	 */
	private boolean echar(int c, StringBuilder builder) throws IOException {
		if (c=='\\') {
			int d = read();
			switch (d) {
			case 't' :
				builder.append('\t');
				return true;
				
			case 'b' :
				builder.append('\b'); // back-space character
				return true;
				
			case 'n' : 
				builder.append('\n'); 
				return true;
				
			case 'r' :
				builder.append('\r');
				return true;
				
			case 'f' :
				builder.append('\f');
				return true;
				
			case '"' :
				builder.append('"');
				return true;
				
			case '\'' :
				builder.append('\'');
				return true;
				
			case '\\' :
				builder.append('\\');
				return true;
			}
			unread(d);
		}
		return false;
	}

	private boolean uchar(int c, StringBuilder builder) throws RDFParseException, IOException {
		
		if (c == '\\') {
			c = read();
			
			if (c !='u' && c!='U') {
				unread(c);
				return false;
			}
			
			
			char[] array=null;
			
			if (c == 'u') {
				char[] tmp = {hex(), hex(), hex(), hex()};
				array = tmp;
				
				
			} else if (c == 'U') {
				char[] tmp = {hex(), hex(), hex(), hex(), hex(), hex(), hex(), hex()};
				array = tmp;
			} 

			String value = new String(array);
			c = Integer.parseInt(value, 16);
			builder.appendCodePoint(c);
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * STRING_LITERAL_LONG_QUOTE	::=	'"""' (('"' | '""')? ([^"\] | ECHAR | UCHAR))* '"""'
	 * </pre>
	 */
	private void string_literal_long_quote(int c, int d, int e, StringBuilder builder) throws RDFParseException, IOException {

		assertEquals('"', c);
		assertEquals('"', d);
		assertEquals('"', e);
		
		while ( stringLongQuoteChar(c=read(), builder));
		assertEquals('"', c);
		read('"');
		read('"');
	}

	private boolean stringLongSingleQuoteChar(int c, StringBuilder builder) throws RDFParseException, IOException {
		
		if (c == '\'') {
			return quoteChar('\'', builder);
		}
		
		if (echar(c, builder) || uchar(c, builder)) {
			return true;
		}
		
		if (c!='\\' ) {
			
			builder.appendCodePoint(c);
			return true;
		}
		
		return false;
	}

	private boolean stringLongQuoteChar(int c, StringBuilder builder) throws RDFParseException, IOException {
		
		if (c == '"') {
			return quoteChar('"', builder);
		}
		
		if (echar(c, builder) || uchar(c, builder)) {
			return true;
		}
		
		if (c!='\\' ) {
			
			builder.appendCodePoint(c);
			return true;
		}
		
		return false;
	}

	private boolean quoteChar(char q, StringBuilder builder) throws IOException {
		int d = read();
		
		if (d != q) {
			// Matched ["][^"]
			unread(d);
			builder.append(q);
			return true;
			
		} else {
			// Matched ["]["]
			
			int e = read();
			if (e != q) {
				// Matched ["]["][^"]
				unread(e);
				builder.append(q);
				builder.append(q);
				return true;
				
				
			} else {
				// Matched ["]["]["]
				// This is the termination of the string, not characters within the string.
				unread(q);
				unread(q);
				return false;
			}
			
		} 
	}

	private void string_literal_single_quote(int c, StringBuilder builder) throws RDFParseException, IOException {

		assertEquals('\'', c);
		
		while ( stringSingleQuoteChar(c=read(), builder));
		assertEquals('\'', c);
	}

	/**
	 * <pre>
	 * 	STRING_LITERAL_SINGLE_QUOTE	::=	"'" ([^#x27#x5C#xA#xD] | ECHAR | UCHAR)* "'" 
	 * </pre>
	 * 
	 * where
	 * 
	 * <pre>
	 *  #x27='
	 *  #x5C=\ 
	 *  #xA=new line 
	 *  #xD=carriage return 
	 * </pre>
	 */
	private boolean stringSingleQuoteChar(int c, StringBuilder builder) throws RDFParseException, IOException {

		if (echar(c, builder) || uchar(c, builder)) {
			return true;
		}
		if (c!='\'' && c!='\\' && c!='\n' && c!='\r') {
			builder.appendCodePoint(c);
			return true;
		}
		
		return false;
	}

	private void string_literal_long_single_quote(int c, int d, int e, StringBuilder builder) throws RDFParseException, IOException {

		final char q = '\'';
		
		assertEquals(q, c);
		assertEquals(q, d);
		assertEquals(q, e);
		
		while ( stringLongSingleQuoteChar(c=read(), builder));
		assertEquals(q, c);
		read(q);
		read(q);
	}

	/**
	 * <pre>
	 * BlankNode	::=	BLANK_NODE_LABEL | ANON
	 * </pre>
	 * @param c
	 * @return
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private BNode tryBlankNode(int c) throws RDFParseException, IOException {
		BNode bnode = null;
		
		return	(bnode=blank_node_label(c)) == null ? anon(c) : bnode;
	}

	/**
	 * <pre>
	 * ANON	::=	'[' WS* ']'
	 * </pre>
	 */
	private BNode anon(int c) throws IOException {
		if (c != '[') {
			return null;
		}
		c = next();
		if (c != ']') {
			unread(c);
			return null;
		}
		
		return valueFactory.createBNode();
	}

	/**
	 * <pre>
	 * BLANK_NODE_LABEL	::=	'_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS)?
	 * </pre>
	 * @throws RDFParseException 
	 */
	private BNode blank_node_label(int c) throws IOException, RDFParseException {
		if (c != '_') {
			return null;
		}
		c = read();
		if (c != ':') {
			unread(c);
			return null;
		}
		
		c = read();
		if (!(pn_chars_u(c) || inRange(c, '0', '9')) ) {
			StringBuilder builder = err();
			builder.append("Invalid initial character for BNode label: '");
			builder.appendCodePoint(c);
			builder.append("'");
			fail(builder);
		}
		StringBuilder builder = buffer();
		builder.appendCodePoint(c);
		
		int last = -1;
		for (;;) {
			c = read();
			if (!(pn_chars(c) || (c=='.') )) {
				break;
			}
			builder.appendCodePoint(c);
			last = c;
		}
		unread(c);
		if (last == '.') {
			unread(last);
		}
		
		return valueFactory.createBNode(builder.toString());
	}

	private URI tryIri(int c) {
		int saveLineNumber = lineNumber;
		int saveColumnNo = columnNumber;
		try {
			return iri(c);
		} catch (Throwable oops) {
			lineNumber = saveLineNumber;
			columnNumber = saveColumnNo;
		}
		return null;
	}

	protected URI verb() throws IOException, RDFParseException, RDFHandlerException {
		int c = next();
		if (c == 'a') {
			int cc = read();
			if (isWhitespace(cc)) {
				return RDF.TYPE;
			}
			unread(cc);
		}
		
		
		return iri(c);
	}

	/**
	 * <pre>
	 * subject	::=	iri | BlankNode | collection
	 * </pre>
	 * @throws RDFHandlerException 
	 */
	protected Resource subject(int c) throws RDFParseException, IOException, RDFHandlerException {
		Resource result = null;
		if (c == '_' || c=='[') {
			result = tryBlankNode(c);
			if (result == null) {
				fail("Invalid BNode definition");
			}
		} else if (c == '(') {
			fail("TODO: Implement collection");
		} else {
			result = iri(c);
		}
		
		if (result == null) {
			StringBuilder msg = err();
			msg.append("Found '");
			msg.appendCodePoint(c);
			msg.append("' but expected (iri | BlankNode | collection)");
			fail(msg);
		}
		
		return result;
	}
	
	protected URI iri() throws RDFParseException, IOException, RDFHandlerException {
		return iri(read());
	}

	/**
	 * iri	::=	IRIREF | PrefixedName
	 * @throws RDFHandlerException 
	 */
	protected URI iri(int c) throws RDFParseException, IOException, RDFHandlerException {
		if (c == '<') {
			String text = iriRef(c);
			return valueFactory.createURI(text);
		} else {
			return prefixedName(c);
		}
	}

	/**
	 * <pre>
	 * PrefixedName	::=	PNAME_LN | PNAME_NS
	 *              ::= (PNAME_NS PN_LOCAL) | PNAME_NS
	 *              ::= PNAME_NS PN_LOCAL?
	 * </pre>
	 */
	protected URI prefixedName(int c) throws IOException, RDFParseException {
		unread(c);
		
		String prefix = pname_ns();
		String localName = pn_local();
		
		String namespace = namespaceMap.get(prefix);
		if (namespace == null) {
			fail("Namespace not defined for prefix '" + prefix + "'");
		}
		
		return valueFactory.createURI(namespace + localName);
	}

	/**
	 * <pre>
	 * PN_LOCAL	::=	(PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
	 * </pre>
	 * @return The string matching the PN_LOCAL rule, or an empty string if no match.
	 */
	public String pn_local() throws IOException, RDFParseException {
		StringBuilder builder = buffer();
		
		int c = read();
		
		if (
			!pn_chars_u(c) &&
			(c != ':') &&
			!inRange(c, '0', '9') &&
			!plx(c)
		) {
			unread(c);
		} else {
			builder.appendCodePoint(c);
			int last = -1;
			for (;;) {
				c = read();
				if (
					!pn_chars(c) &&
					(c != '.') &&
					(c != ':') &&
					!plx(c)
				) {
					break;
				}
				
				builder.appendCodePoint(c);
				last = c;
				
			}
			unread(c);
			if (last == '.') {
				unread(c);
			}
		}
		
		return builder.toString();
	}

	/**
	 * <pre>
	 * PLX	::=	PERCENT | PN_LOCAL_ESC
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	protected boolean plx(int c) throws IOException, RDFParseException {
		return percent(c) || pn_local_esc(c);
	}

	/**
	 * <pre>
	 * pn_local_esc :== '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
	 * </pre>
	 */
	private boolean pn_local_esc(int c) throws IOException {
		if (c != '\\') {
			return false;
		}
		int next = read();
		
		switch (next) {
		case '_' : 
		case '~' : 
		case '.' : 
		case '-' : 
		case '!' : 
		case '$' : 
		case '&' : 
		case '\'' : 
		case '(' : 
		case ')' : 
		case '*' : 
		case '+' : 
		case ',' : 
		case ';' : 
		case '=' : 
		case '/' : 
		case '?' : 
		case '#' : 
		case '@' : 
		case '%' :
			buffer.appendCodePoint(c);
			buffer.appendCodePoint(next);
			return true;
		}
		unread(next);
		
		return false;
	}

	/**
	 * <pre>
	 * PERCENT	::=	'%' HEX HEX
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private boolean percent(int c) throws RDFParseException, IOException {
		if (c != '%') {
			return false;
		}
		
		char[] array = {hex(), hex()};
		String text = new String(array);
		c = Integer.parseInt(text, 16);
		buffer.appendCodePoint(c);
		return true;
	}
	
	protected IriTemplate iriTemplate() throws RDFParseException, IOException {
		int c = next();
		String text = iriRef(c);
		return new IriTemplate(text);
	}
	
	protected String iriRef() throws IOException, RDFParseException {
		skipSpace();
		int c = read();
		return iriRef(c);
	}

	protected String iriRef(int c) throws IOException, RDFParseException {

		assertEquals('<', c);
		StringBuilder builder = buffer();

		c = read();
		while (c != -1 && c!='>') {
			if (c == '\\') {
				uchar();
			} else {
				builder.appendCodePoint(c);
			}
			c=read();
		}
		assertEquals('>', c);
		
		return builder.toString();
	}
	
	/**
	 * Read UCHAR, assuming we have already consumed the leading backslash.
	 * 
	 * <pre>
	 * UCHAR ::= '\\u' HEX HEX HEX HEX | '\\U' HEX HEX HEX HEX HEX HEX HEX HEX
	 * </pre>
	 * 
	 * @throws RDFParseException
	 * @throws IOException
	 */
	private void uchar() throws RDFParseException, IOException {
		
		int c = read();
		char[] array=null;
		
		if (c == 'u') {
			char[] tmp = {hex(), hex(), hex(), hex()};
			array = tmp;
			
			
		} else if (c == 'U') {
			char[] tmp = {hex(), hex(), hex(), hex(), hex(), hex(), hex(), hex()};
			array = tmp;
		} else {
			StringBuilder builder = err();
			builder.append("Invalid escape sequence in IRI value. Expected (\\u HEX HEX HEX | \\U HEX HEX HEX HEX HEX HEX HEX HEX) but found: \\");
			builder.appendCodePoint(c);
			fail(builder);
		}

		String value = new String(array);
		c = Integer.parseInt(value, 16);
		buffer.appendCodePoint(c);
		
		
	}

	protected char hex() throws IOException, RDFParseException {
		int c = read();
		if (!inRange(c, 'a', 'f') &&
			!inRange(c, 'A', 'F') &&
			!inRange(c, '0', '9')
		) {
			StringBuilder msg = err();
			msg.append("Invalid HEX value. Expected  [a-f] | [A-F] | [0-9] but found '");
			msg.appendCodePoint(c);
			msg.append("'");
			fail(msg);
		}
		
		return (char) c;
	}

	protected StringBuilder err() {
		buffer.setLength(0);
		buffer.append("Line ");
		buffer.append(lineNumber);
		if (columnNumber > 0) {
			buffer.append(':');
			buffer.append(columnNumber-1);
		}
		buffer.append(' ');
		
		return buffer;
	}

	protected void fail(String msg) throws RDFParseException {
		
		StringBuilder builder = err();
		builder.append(msg);
		fail(builder);
				
		
	}
	protected void fail(StringBuilder builder) throws RDFParseException {
		throw new RDFParseException(builder.toString());
	}

	protected int next() throws IOException {
		int c = read();
		
		while (isWhitespace(c)) {
			c = read();
		}
		
		return c;
		
	}

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	@Override
	public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
		InputStreamReader reader = new InputStreamReader(in);
		parse(reader, baseURI);
	}
	
	protected void initParse(Reader reader, String baseURI) {
		if (this.reader != reader) {
			this.reader = new PushbackReader(reader, 20);
		}
		this.baseURI = baseURI;
	}

	@Override
	public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
		initParse(reader, baseURI);
		turtleDoc();
	}
	
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
	
	protected boolean isWhitespace(int c) {
		return c==' ' || c=='\t' || c=='\r' || c=='\n';
	}
	

	

	protected boolean tryWhitespace() throws IOException {
		int c = read();
		if (isWhitespace(c)) {
			skipSpace();
			return true;
		}
		unread(c);
		return false;
	}
	protected boolean tryWord(String text) throws IOException {
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			int k = read();
			if (k != c) {
				unread(k);
				for (int j=i-1; j>=0; j--) {
					unread(text.charAt(j));
				}
				return false;
			}
		}
		return true;
	}

	protected void read(char c) throws IOException, RDFParseException {
		
		int k = next();
		assertEquals(c, k);
	}
	
	protected int peek() throws IOException {
		int c = read();
		unread(c);
		return c;
	}
}
