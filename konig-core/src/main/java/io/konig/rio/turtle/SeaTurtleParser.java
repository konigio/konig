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
import java.io.Reader;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.ChainedContext;

public class SeaTurtleParser extends TurtleParser {
	
	private Context defaultContext;
	private Context currentContext;
	private ContextHandler contextHandler;
	
	private Term lastTerm = null;
	private Term predicateTerm = null;
	
	public SeaTurtleParser() {
		super((NamespaceMap) null, ValueFactoryImpl.getInstance());
		currentContext = defaultContext = new ChainedContext(null, new BasicContext(null));
		namespaceMap = new ContextNamespaceMap();
		setValueFactory(new CoercingValueFactory());
	}
	
	@Override
	public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
		
		currentContext = defaultContext;
		super.parse(reader, baseURI);
	}

	public ContextHandler getContextHandler() {
		return contextHandler;
	}

	public void setContextHandler(ContextHandler contextHandler) {
		this.contextHandler = contextHandler;
	}



	/**
	 * <pre>
	 *  prefixID | base | sparqlPrefix | sparqlBase | context
	 * </pre>
	 */
	protected void directive(int c) throws IOException, RDFParseException, RDFHandlerException {
		
		
		if (tryWord("prefix")) {
			prefixID();
			
		} else if (tryWord("context")) {
			contextTermList();
			
		} else if (tryWord("base")) {
			base();
		} 
		
		
	}



	/**
	 * <pre>
	 * contextTermList ::= '{' contextElement (',' contextElement)* '}' 
	 *                 ::= '{' term ( ',' term)* '}'
	 * </pre>
	 */
	private void contextTermList() throws IOException, RDFParseException {
		assertNext('{');
		
		Context parentContext = currentContext;
		
		Context context = new BasicContext(null);
		currentContext = new ChainedContext(parentContext, context);
		
		Term term = term();
		context.add(term);
		
		int c = ws();
		while (c == ',') {
			term = term();
			context.add(term);
			c = ws();
		}
		assertEquals('}', c);
		
		currentContext.compile();
		if (contextHandler != null) {
			contextHandler.addContext((ChainedContext) currentContext);
		}
	}

	/**
	 * Here's the official Turtle 1.1 Syntax
	 * <pre>
	 * PrefixedName	::=	PNAME_LN | PNAME_NS
	 *              ::= (PNAME_NS PN_LOCAL) | PNAME_NS
	 *              ::= PNAME_NS PN_LOCAL?
	 *              ::= PN_PREFIX? ':' PN_LOCAL?      
	 * </pre>
	 * 
	 * We customize the Turtle syntax by redefining PrefixedName as follows.
	 * <pre>
	 * PrefixedName	::=	(PN_PREFIX? ':' PN_LOCAL?) | bareLocalName 
	 * bareLocalName ::= PN_PREFIX
	 * </pre>
	 * Notice that this customization requires that a bareLocalName is allowed only if it matches
	 * the syntax of a namespace prefix.
	 */
	protected URI prefixedName(int c) throws IOException, RDFParseException {
		unread(c);
		
		String prefix = pn_prefix();
		
		c = read();
		
		if (c != ':') {
			unread(c);
			// Treat the prefix as a bare local name.
			
			defaultContext.compile();
			
			Term term = lastTerm = currentContext.getTerm(prefix);
			if (term == null) {
				StringBuilder err = err();
				err.append("Term not defined: ");
				err.append(prefix);
				fail(err);
			}
			
			return  term.getExpandedId();
		}
		
		String localName = pn_local();

		String namespace = namespaceMap.get(prefix);
		if (namespace == null) {
			Term term = currentContext.getTerm(prefix);
			if (term != null) {
				defaultContext.compile();
				namespace = term.getExpandedIdValue();
			}
		}
		if (namespace == null) {
			fail("Namespace not defined for prefix '" + prefix + "'");
		}
		
		return valueFactory.createURI(namespace + localName);
	}

	/**
	 * <pre>
	 * term ::= termName ':' termDefinition
	 * </pre>
	 */
	private Term term() throws IOException, RDFParseException {
		String termName = termName();
		assertNext(':');
		
		return termDefinition(termName);
	}

	/**
	 * <pre>
	 * termName ::= jsonString
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private String termName() throws RDFParseException, IOException {
		return jsonString();
	}

	/**
	 * <pre>
	 *	jsonString ::= '"' jsonChar* '"'
	 *</pre>
	 */
	private String jsonString() throws RDFParseException, IOException {
		assertNext('"');
		buffer();
		
		while (jsonChar());
		
		String result = buffer.toString();
		assertNext('"');
		return result;
	}

	/**
	 * <pre>
	 * jsonChar ::= unescapedJsonChar | escapedJsonChar
	 * </pre>
	 */
	private boolean jsonChar() throws IOException, RDFParseException {
		int c = read();
		boolean ok = unescapedJsonChar(c) || escapedJsonChar(c);
		if (!ok) {
			unread(c);
		}
		return ok;
	}

	/**
	 * <pre>
	 *
	 * escapedJsonChar ::= escape (
	 *	  [#x22] 
	 *	| [#x5C] 
	 *	| [#x2F]
	 *	| [#x62]
	 *	| [#x66]
	 *	| [#x6E]
	 *	| [#x72]
	 *	| [#x74]
	 *	| 'u' hex hex hex hex)
	 *
	 * escape := [#xx5C]
	 * </pre>
	 */
	private boolean escapedJsonChar(int c) throws IOException, RDFParseException {
		if (c=='\\') {
			c=read();
			
			switch (c) {
			case 0x5C :
			case 0x2F :
			case 0x62 :
			case 0x66 :
			case 0x6E :
			case 0x72 :
			case 0x74 :
				buffer.appendCodePoint(c);
				break;
				
			case 'u' :
				String hexString = new String(new char[]{hex(), hex(), hex(), hex()});
				buffer.appendCodePoint(Integer.parseInt(hexString, 16));
				break;
				
			default :
				fail("Invalid escape character.  Expected '\\\"', '\\\\', '\\/', '\\b', '\\f', '\\n', '\\r', '\\t' or '\\u' hex hex hex .");
				
			}
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * unescapedJsonChar ::= [#x20-#x21] | [#x23-#x5B] | [#x4D-#x10FFF]
	 * </pre>
	 */
	private boolean unescapedJsonChar(int c) {
		if (
			inRange(c, 0x20, 0x21) ||
			inRange(c, 0x23, 0x5B) ||
			inRange(c, 0x5D, 0x10FFFF)
		) {
			buffer.appendCodePoint(c);
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * termDefinition ::= jsonString | expandedTermDefinition
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private Term termDefinition(String termName) throws IOException, RDFParseException {
		Term term = null;
		int c = peek();
		if (c == '"') {
			String idValue = jsonString();
			term = new Term(termName, idValue, Kind.ANY);
		} else if (c == '{') {
			term = expandedTermDefinition(termName);
		}
		return term;
	}

	/**
	 * <pre>
	 * expandedTermDefinition ::= '{' termElement (',' termElement)* '}'
	 * termElement ::= 
  	 *    idProperty
	 *	| typeProperty
	 *  | languageProperty
	 *
	 * idProperty ::=  '"@id"'  ':' jsonString	
	 * typeProperty ::=  '"@type"' ':' jsonString
	 * languageProperty ::= '"@language"' ':' jsonString
	 * </pre>
	 */
	private Term expandedTermDefinition(String termName) throws RDFParseException, IOException {
		
		String id=null;
		String type=null;
		String language=null;
		
		assertNext('{');
		for (;;) {
			
			String keyword = jsonString();
			
			switch (keyword) {
			case "@id" : 
				assertNext(':');
				id = jsonString();
				break;
				
			case "@type" :
				assertNext(':');
				type = jsonString();
				break;
				
			case "@language" :
				assertNext(':');
				language = jsonString();
				break;
				
			default :
				StringBuilder err = err();
				err.append("JSON-LD keyword not supported: ");
				err.append(keyword);
				fail(err);
			}
			
		
			int c = ws();
			if (c == '}') {
				break;
			}
			if (c!=',') {
				fail("Expected ',' or '}'");
			}
		}
		return new Term(termName, id, language, type);
	}
	
	/**
	 * <pre>
	 * iri	::=	IRIREF | PrefixedName | BareLocalName | IriTemplate | IriPropertyList
	 * </pre>
	 * @throws RDFHandlerException 
	 */
	protected URI iri(int c) throws RDFParseException, IOException, RDFHandlerException {
		if (c == '<') {
			String text = iriRef(c);
			return valueFactory.createURI(text);
		} else if (c=='{') {
			return iriPropertyList(c);
		} else {
			return prefixedName(c);
		}
	}


	/**
	 * <pre>
	 * IriPropertyList ::= '{' context? idProperty predicateObjectList '}'
	 * </pre>
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 */
	private URI iriPropertyList(int c) throws RDFParseException, IOException, RDFHandlerException {
		assertEquals('{', c);
		
		Context initialContext = currentContext;
		
		URI id = null;
		c = ws();
		while (c=='@') {
			
			if (tryWord("context")) {
				if (id != null) {
					fail("@context must come before @id");
				}
				contextTermList();
			} else if (tryWord("id")) {
				id = iri(ws());
			} else {
				fail("Expected '@context' or '@id'");
			}
			c = ws();
		}

		if (id == null) {
			fail("@id property must be defined.");
		}
		
		if (c == ';') {
			predicateObjectList(id);
			assertNext('}');
		} else {
			assertEquals('}', c);
		}
		
		if (currentContext != initialContext) {
			ChainedContext chain = (ChainedContext) currentContext;
			currentContext = chain.getParent();
			if (contextHandler != null) {
				contextHandler.removeContext(chain);
			}
		}
		
		return id;
	}

	protected URI verb() throws IOException, RDFParseException, RDFHandlerException {
		

		// We capture the lastTerm as a side-effect of invoking the verb() rule.
		// And we save the lastTerm as the predicateTerm, so that we can use it later
		// for type coercion 
		
		lastTerm = null;
		URI result = super.verb();
		predicateTerm = lastTerm;
		
		return result;
	}
	
	protected void triples(int c) throws RDFParseException, IOException, RDFHandlerException {
		Resource subject = null;
		if (c == '[') {
			subject = tryBlankNodePropertyList(c);
			if (subject != null) {

				c = ws();
				unread(c);
				if (c != '.') {
					predicateObjectList(subject);
				}
				return;
			}
		}  else if (c== '{') {
			subject = iriPropertyList(c);
			
			c = ws();
			unread(c);
			if (c != '.') {
				predicateObjectList(subject);
			}
			return;
		}
		
		subject = subject(c);
		predicateObjectList(subject);
		
	}
	
	private class ContextNamespaceMap implements NamespaceMap {
		
		@Override
		public String get(String prefix) {
			defaultContext.compile();
			Term term = currentContext.getTerm(prefix);
			
			return term==null ? null : term.getExpandedIdValue();
		}

		@Override
		public void put(String prefix, String name) {
			defaultContext.addTerm(prefix, name).setKind(Kind.NAMESPACE);
		}
		
	}
	
	private class CoercingValueFactory extends ValueFactoryImpl {
		@Override
		public Literal createLiteral(String value, URI datatype) {
			if (predicateTerm != null) {
				currentContext.compile();
				
				String lang = predicateTerm.getLanguage();
				if (lang != null) {
					return super.createLiteral(value, lang);
				}
				
				URI type = predicateTerm.getExpandedType();
				if (type != null) {
					datatype = type;
				}
			}
			String lang = currentContext.getLanguage();
			if (lang != null && XMLSchema.STRING.equals(datatype)) {
				return super.createLiteral(value, lang);
			}
			return super.createLiteral(value, datatype);
		}
	}
}
