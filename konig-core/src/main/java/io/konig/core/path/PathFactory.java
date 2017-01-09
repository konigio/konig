package io.konig.core.path;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.antlr.path.PathBaseListener;
import io.konig.antlr.path.PathLexer;
import io.konig.antlr.path.PathParser;
import io.konig.core.LocalNameService;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;	

public class PathFactory {
	
	private NamespaceManager nsManager;
	private LocalNameService localNameService;
	
	public PathFactory(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}
	
	
	public PathFactory(NamespaceManager nsManager, LocalNameService localNameService) {
		this.nsManager = nsManager;
		this.localNameService = localNameService;
	}




	public Path createPath(String text) throws PathParseException {
		CharStream stream = new ANTLRInputStream(text);		
		PathLexer lexer = new PathLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PathParser parser = new PathParser(tokens);
		parser.setTrace(true);
		Listener listener = new Listener();
		parser.addParseListener(listener);
		
		parser.path();
		
		return listener.getPath();
	}
	
	
	class Listener extends PathBaseListener {
		private PathImpl path;
		private URI iri = null;
		private URI hasPredicateIRI = null;
		private String localName = null;
		private String prefix = null;
		private Literal literal = null;
		private String stringContent = null;
		private String languageCode = null;
		private URI stringType = null;
		
		
		public Path getPath() {
			return path;
		}

		@Override
		public void enterPath(PathParser.PathContext ctx) {
			path = new PathImpl();
		}
		
		@Override
		public void exitStart(PathParser.StartContext ctx) {
			path.v(iri);
			iri = null;
		}
		
		
		@Override
		public void exitOut(PathParser.OutContext ctx) {
			path.out(iri);
			iri = null;
		}
		
		@Override
		public void exitIn(PathParser.InContext ctx) {
			path.in(iri);
			iri = null;
		}
		
		@Override
		public void exitRawIri(PathParser.RawIriContext ctx) {
			iri = new URIImpl(ctx.getText());
		}
		
		@Override
		public void exitHasValue(PathParser.HasValueContext ctx) {
			if (literal != null) {
				path.has(hasPredicateIRI, literal);
				literal = null;
			} else if (iri != null) {
				path.has(hasPredicateIRI, iri);
				iri = null;
			}
		}
		
		@Override
		public void exitNumericLiteral(PathParser.NumericLiteralContext ctx) {
			String text = ctx.getText();
			if (text.indexOf('.')>=0) {
				literal = new LiteralImpl(text, XMLSchema.DOUBLE);
			} else {
				literal = new LiteralImpl(text, XMLSchema.INTEGER);
			}
			
		}
		
		
		@Override
		public void exitHasStep(PathParser.HasStepContext ctx) {
			hasPredicateIRI = null;
		}
		
		@Override
		public void exitHasPredicate(PathParser.HasPredicateContext ctx) {
			hasPredicateIRI = iri;
			iri = null;
		}
		
		
		
		@Override
		public void exitQname(PathParser.QnameContext ctx) {
			Namespace ns = nsManager.findByPrefix(prefix);
			if (ns == null) {
				throw new PathParseException("Namespace prefix not defined: " + prefix);
			}
			
			String value = ns.getName() + localName;
			iri = new URIImpl(value);

			prefix = localName = null;
		}
		
		@Override
		public void exitLocalName(PathParser.LocalNameContext ctx) {
			localName = ctx.getText();
		}
		
		@Override
		public void exitPrefix(PathParser.PrefixContext ctx) {
			prefix = ctx.getText();
		}
		
		@Override
		public void exitBareLocalName(PathParser.BareLocalNameContext ctx) {
			if (localNameService == null) {
				throw new PathParseException("Bare local name not supported");
			}
			String value = ctx.getText();
			Set<URI> set = localNameService.lookupLocalName(value);
			if (set.isEmpty()) {
				throw new PathParseException("Local name not found: " + value);
			}
			if (set.size()>1) {
				throw new PathParseException("Local name is ambiguous: " + value);
			}
			iri = set.iterator().next();
		}
		
		@Override
		public void exitStringContent(PathParser.StringContentContext ctx) {
			stringContent = ctx.getText();
		}
		
		@Override
		public void exitLanguageCode(PathParser.LanguageCodeContext ctx) {
			languageCode = ctx.getText();
		}
		
		@Override
		public void exitStringType(PathParser.StringTypeContext ctx) {
			stringType = iri;
			iri = null;
		}
		
		@Override
		public void exitBooleanLiteral(PathParser.BooleanLiteralContext ctx) {
			String text = ctx.getText();
			literal = "true".equals(text) ? BooleanLiteralImpl.TRUE : BooleanLiteralImpl.FALSE;
		}
		
		@Override
		public void exitStringLiteral(PathParser.StringLiteralContext ctx) {
			if (stringType != null) {
				literal = new LiteralImpl(stringContent, stringType);
				stringType = null;
			} else if (languageCode != null) {
				literal = new LiteralImpl(stringContent, languageCode);
				languageCode = null;
			} else {
				literal = new LiteralImpl(stringContent);
			}
			stringContent = null;
		}
	}

}