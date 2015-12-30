package io.konig.core.io;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import static io.konig.core.io.GraphConstants.BNODE;
import static io.konig.core.io.GraphConstants.IRI;
import static io.konig.core.io.GraphConstants.GRAPH;
import static io.konig.core.io.GraphConstants.LANG;
import static io.konig.core.io.GraphConstants.LITERAL_IRI;
import static io.konig.core.io.GraphConstants.LITERAL_QNAME;
import static io.konig.core.io.GraphConstants.LITERAL_TERM;
import static io.konig.core.io.GraphConstants.PLAIN;
import static io.konig.core.io.GraphConstants.QNAME;
import static io.konig.core.io.GraphConstants.RESOURCE;
import static io.konig.core.io.GraphConstants.TERM;
import static io.konig.core.io.GraphConstants.VERSION;
import static io.konig.core.io.GraphConstants.LABEL;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.Term;
import io.konig.core.impl.KonigLiteral;


abstract public class BaseGraphReader {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseGraphReader.class);
	protected Context context;
	private ByteBuffer data;
	private Resource subject;
	private URI predicate;
	private Value object;
	
	protected Term qnameTerm;
	protected Term term;
	
	

	/**
	 * Read binary data into a specified Graph.
	 * @param data The data that is to be read
	 * @param graph The graph into which the data will be read
	 * @param manager A ContextManager that can be used to fetch the Context that governs the data
	 * @throws IOException
	 */
	protected void read(byte[] data, ContextManager manager) throws KonigReadException {
		
		this.data = ByteBuffer.wrap(data);
		
		short version = readVersion();
		
		if (version != VERSION) {
			throw new KonigReadException("Unsupported version " + version);
		}
		
		String contextURI = readString();
		
		context = manager.getContextByURI(contextURI);
		if (context == null) {
			throw new KonigReadException("Context not found: " + contextURI);
		}
		context.compile();
		
		startRDF();
		handleContext(context);		
		readVertices();
		endRDF();
		
	}

	
	protected void startRDF() {
		
	}
	
	protected void handleContext(Context context)  {
		
	}
	
	protected void beginSubject(Resource subject, Term qname, Term idTerm) {
		
	}

	protected void endSubject() {
		
	}
	

	
	protected void startPredicate(URI predicate, Term term, Term qnameTerm, int objectCount) {
		
	}
	
	protected void endPredicate(URI predicate, int objectCount) {
		
	}

	protected void handleObject(Value value, Term term, Term qnameTerm) {
		
	}
	
	protected void handleStatement(Resource subject, URI predicate, Value object) {
		
	}
	
	protected void endRDF() {
		
	}
	
	private void readVertices() {
		
		
		while (peekToken() != 0) {
			
			subject = readResource();
		
			
			beginSubject(subject, term, qnameTerm);
			term = qnameTerm = null;
			
			readProperties();
			readNamedGraph();
			
			endSubject();
			
		}
		
	}
	
	protected Object beginNamedGraph(Resource subject) {
		return null;
	}
	
	protected void endNamedGraph(Object state) {
		
	}
	
	private void readNamedGraph() {
	
		byte token = peekToken();
		if (token == GRAPH) {
			data.get();
			
			Resource oldSubject = subject;
			Object oldState = beginNamedGraph(subject);
			
			short count = data.getShort();
			
			logger.debug("READ: GRAPH {} count={}", subject.stringValue(), count);
			
			for (int i=0; i<count; i++) {

				subject = readResource();
				
				beginSubject(subject, term, qnameTerm);
				term = qnameTerm = null;
				
				readProperties();
				readNamedGraph();
				
				endSubject();
			}
			
			subject = oldSubject;
			endNamedGraph(oldState);
		}
		
	}


	private Resource readResource() {
		byte token = peekToken();
		
		switch (token) {
		case TERM :
		case QNAME: 
		case IRI:
			return readIRI();
			
		case BNODE:
			return readBNode();
			
		default:
			throw new KonigReadException("Invalid token for Resource: " + token);
		}
	}

	private void readProperties() {
		
		int count = data.getShort();
		for (int i=0; i<count; i++) {
			
			predicate = readIRI();
			readObjects();
		}
		
	}

	private void readObjects() {

		int count = data.getShort();
		
		startPredicate(predicate, term, qnameTerm, count);
		term = qnameTerm = null;
		
		for (int i=0; i<count; i++) {
			
			object = readObject();
			
			handleObject(object, term, qnameTerm);
			term = qnameTerm = null;
			
			logger.debug("READ: {} {} {}", subject, predicate, object);
			handleStatement(subject, predicate, object);
		}
		
		endPredicate(predicate, count);
		
		
		
	}

	private Value readObject() {
		int token = peekToken();
		
		switch (token) {
/* This block was added in haste to work around a bug. TODO: Confirm this makes sense */
		case TERM :
		case QNAME: 
		case IRI:
			return readIRI();
/* End questionable block */
			
		case RESOURCE 		: return resourceReference();
		case BNODE    		: return readBNode();
		case PLAIN			: return readPlainLiteral();
		case LANG			: return readLanguageLiteral();
		case LITERAL_TERM 	: return readTermLiteral();
		case LITERAL_QNAME	: return readQNameLiteral();
		case LITERAL_IRI	: return readIRILiteral();
		
		}
		
		throw new KonigReadException("Invalid token for object " + token);
	}

	
	
	private Value readIRILiteral() {
		assertToken(LITERAL_IRI);
		return new URIImpl(readString());
	}

	private Value readQNameLiteral() {
		assertToken(LITERAL_QNAME);
		return qname();
	}
	
	private URI qname() {

		int index = data.getShort();
		List<Term> list = context.asList();
		if (index >= list.size()) {
			throw new KonigReadException("Index for term in context out of range");
		}
		qnameTerm = list.get(index);
		String namespaceValue = null;
		URI namespace = qnameTerm.getExpandedId();
		if (namespace != null) {
			namespaceValue = namespace.stringValue();
		} else {
			namespaceValue = qnameTerm.getId();
			if (namespaceValue == null) {
				throw new KonigReadException("Namespace not found for term " + qnameTerm.getKey());
			}
		}
		
		
		String localName = readString();
		return new URIImpl(namespaceValue + localName);
	}

	private Value readTermLiteral() {
		assertToken(LITERAL_TERM);
		URI type = term();
		String value = readString();
		
		return new KonigLiteral(value, type);
	}

	private Value readLanguageLiteral() {
		assertToken(LANG);
		String language = readString();
		String text = readString();
		return new KonigLiteral(text, language);
	}

	private Value readPlainLiteral() {
		assertToken(PLAIN);
		String text = readString();
		return new KonigLiteral(text);
	}

	private Resource resourceReference() {
		assertToken(RESOURCE);
		int position = data.getInt();
		int mark = data.position();
		data.position(position);
		Resource result = readResource();
		data.position(mark);
		
		return result;
	}

	private void assertToken(byte expected) {
		int token = data.get();
		if (token != expected) {
			throw new KonigReadException("Expected token " + expected + " but found " + token);
		}
		
	}

	private BNode readBNode() {
		assertToken(BNODE);
		int bnodeId = data.getShort();
		return new BNodeImpl("x" + bnodeId);
	}

	private URI readIRI() {
		int token = peekToken();
		switch (token) {
		case TERM :
			return readTerm();
			
		case QNAME:
			return readQName();
			
		case IRI:
			return absoluteIRI();
			
		default:
			throw new KonigReadException("Invalid IRI token: " + token);
		}
	}

	private URI absoluteIRI() {
		assertToken(IRI);
		
		return new URIImpl(readString());
	}

	private URI readQName() {
		assertToken(QNAME);
		return qname();
	}

	private URI readTerm() {
		assertToken(TERM);
		return term();
	}
	
	private URI term() {

		int index = data.getShort();
		List<Term> list = context.asList();
		if (index >= list.size()) {
			throw new KonigReadException("Index for term in context out of range");
		}
		term  = list.get(index);
		URI result = term.getExpandedId();
		if (result == null) {
			String id = term.getId();
			if (id == null) {
				throw new KonigReadException("Expanded value for term not found: " + term.getKey());
			}
			result = new URIImpl(id);
			
		}
		return result;
	}

	private byte peekToken() {
		if (!data.hasRemaining()) {
			return 0;
		}
		int mark = data.position();
		byte token = data.get();
		data.position(mark);
		return token;
	}

	private String readString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (data.hasRemaining()) {
			byte b = data.get();
			if (b == 0) {
				break;
			}
			out.write(b);
		}
		return new String(out.toByteArray());
	}

	private short readVersion() {
		return data.getShort();
	}
	

}
