package io.konig.core.path;

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
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.LocalNameService;
import io.konig.core.NameMap;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.rio.turtle.SeaTurtleParser;

public class PathParser extends SeaTurtleParser {
	
	private NamespaceManager nsManager;

	public PathParser(NamespaceManager nsManager) {
		this(nsManager==null ? null : new NamespaceMapAdapter(nsManager), null);
		this.nsManager = nsManager;
	}
	
	public PathParser(NamespaceMap map, PushbackReader reader) {
		super(map);
		setRDFHandler(new Handler());
		this.reader = reader;
	}

	public PathParser(NameMap nameMap) {
		this.nameMap = nameMap;
	}

	public Path path(Reader input) throws PathParseException {
		
		super.initParse(input, "");
		
		return path();
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
//	protected URI prefixedName(int c) throws IOException, RDFParseException {
//		unread(c);
//		
//		String prefix = pn_prefix();
//		
//		c = read();
//		
//		if (c != ':') {
//			unread(c);
//			// Treat the prefix as a bare local name.
//			if (localNameService != null) {
//				Set<URI> set = localNameService.lookupLocalName(prefix);
//				if (set.isEmpty()) {
//					StringBuilder err = err();
//					err.append("No URI found with local name '");
//					err.append(prefix);
//					err.append("'");
//					fail(err);
//				}
//				
//				if (set.size()>1) {
//					StringBuilder err = err();
//					err.append("Local name '");
//					err.append(prefix);
//					err.append("' is ambgious. Matching values include ");
//					
//					int count = 0;
//					for (URI uri : set) {
//						if (count > 0) {
//							err.append(", ");
//						}
//						err.append('<');
//						err.append(uri.stringValue());
//						err.append('>');
//						count++;
//						if (count >= 3) {
//							break;
//						}
//					}
//					if (set.size()>3) {
//						err.append(" ...");
//					}
//					fail(err);
//				}
//				return set.iterator().next();
//			} else {
//				
//				StringBuilder err = err();
//				err.append("Bare local names not supported. ");
//				err.append("Use a fully-qualified IRI or a prefixed name, ");
//				err.append("or assign a LocalNameService to this PathParser.");
//				fail(err);
//			}
//
//		}
//		
//		String localName = pn_local();
//
//		String namespace = namespaceMap.get(prefix);
//		if (namespace == null) {
//			fail("Namespace not defined for prefix '" + prefix + "'");
//		}
//		
//		return valueFactory.createURI(namespace + localName);
//	}
	
	/**
	 * <pre>
	 * path ::= step+
	 * step ::= namedIndividual | in | out | filter
	 * </pre>
	 * 
	 * @param path
	 */
	private Path path() throws PathParseException {
		Path path = new PathImpl();
		try {
			prologue();
			path.setContext(getContext());
			int c;
			while (!done(c=next())) {
				unread(c);
				Step step=null;
				switch (c) {
				case '/' :
					step = out();
					break;
					
				case '^' :
					step = in();
					break;
					
				case '[' :
					step = filter();
					break;
					
				default :
					step = namedIndividual();
				}
				path.asList().add(step);
			}
		
		} catch (IOException | RDFParseException | RDFHandlerException e) {
			throw new PathParseException(e);
		}
		if (nameMap != null ) {
			Context context = path.getContext();
			if (context.asList().isEmpty() && nsManager != null) {
				PathContextBuilder.buildContext(path, nsManager);
			}
		}
		return path;
	}




	protected boolean done(int c) throws IOException {
		return c==-1;
	}

	/**
	 * <pre>
	 * namedIndividual ::= iri
	 * </pre>
	 * @throws RDFHandlerException 
	 */
	private Step namedIndividual() throws RDFParseException, IOException, RDFHandlerException {
		URI iri = iri();
		return new VertexStep(iri);
	}
	
	private Handler handler() {
		return (Handler) getRDFHandler();
	}

	/**
	 * <pre>
	 * filter ::= blankNodePropertyList
	 * </pre>
	 * 
	 * The <code>blankNodePropertyList</code> rule is defined in the
	 * <a href="https://www.w3.org/TR/turtle/#sec-grammar-grammar">Turtle 1.1</a> specification.
	 */
	private Step filter() throws RDFParseException, RDFHandlerException, IOException {
		BNode bnode = blankNodePropertyList();
		if (bnode == null) {
			StringBuilder err = err();
			err.append("BNode property list cannot be empty");
			fail(err);
		}
		BlankNodeStep top = handler().peek();
		return top.step;
	}

	/**
	 * <pre>
	 * in ::= '^' iri
	 * </pre>
	 * @throws RDFHandlerException 
	 */
	private Step in() throws RDFParseException, IOException, RDFHandlerException {
		read('^');
		URI predicate = iri();
		return new InStep(predicate);
	}

	/**
	 * <pre>
	 * out ::= '/' iri
	 * </pre>
	 * @throws RDFHandlerException 
	 */
	private Step out() throws RDFParseException, IOException, RDFHandlerException {
		read('/');
		URI predicate = iri();
		return new OutStep(predicate);
	}


	private static class Handler extends RDFHandlerBase {
		
		List<BlankNodeStep> stack = new ArrayList<>();
		
		@Override
		public void handleStatement(Statement st) throws RDFHandlerException {
			
			BlankNodeStep top = peek();
			
			Resource subject = st.getSubject();
			URI predicate = st.getPredicate();
			Value object = st.getObject();
			

			if (!(subject instanceof BNode)) {
				throw new RDFHandlerException(
					"Expected subject to be a BNode, but found: " + 
					subject.toString());
			}
			
			BNode bnode = (BNode) subject;
			
			if (top == null) {
				top = new BlankNodeStep(bnode, new HasStep());
				stack.add(top);
			}
			
			if (bnode != top.bnode) {
				throw new RDFHandlerException("Nested BNodes not supported yet.");
				// TODO: support nested BNodes.
			}
			top.step.add(predicate, object);
			
			
		}

		private BlankNodeStep peek() {
			
			return stack.isEmpty() ? null : stack.get(stack.size()-1);
		}
	}
	
	private static class BlankNodeStep {
		private BNode bnode;
		private HasStep step;
		public BlankNodeStep(BNode bnode, HasStep step) {
			this.bnode = bnode;
			this.step = step;
		}
		
	}



}
