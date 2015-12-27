package io.konig.core.io;

import static io.konig.core.io.GraphConstants.BNODE;
import static io.konig.core.io.GraphConstants.IRI;
import static io.konig.core.io.GraphConstants.LABEL;
import static io.konig.core.io.GraphConstants.LANG;
import static io.konig.core.io.GraphConstants.LITERAL_IRI;
import static io.konig.core.io.GraphConstants.LITERAL_QNAME;
import static io.konig.core.io.GraphConstants.LITERAL_TERM;
import static io.konig.core.io.GraphConstants.PLAIN;
import static io.konig.core.io.GraphConstants.QNAME;
import static io.konig.core.io.GraphConstants.RESOURCE;
import static io.konig.core.io.GraphConstants.GRAPH;
import static io.konig.core.io.GraphConstants.TERM;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.Term;

public class DebugGraphReader  {

	private static final char newline = '\n';
	
	private ByteBuffer data;
	private Context context;
	private StringBuilder builder;

	public String toDebugString(byte[] array, ContextManager manager)  {


		data = ByteBuffer.wrap(array);
		
		builder = new StringBuilder();
		
		short version = data.getShort();
		
		builder.append("0 VERSION ").append(version).append(newline);
		
		mark();		
		String contextURI = readString();
		append("CONTEXT ").append(contextURI).append(newline);
		

		context = manager.getContextByURI(contextURI);
		if (context == null) {
			throw new KonigReadException("Context not found: " + contextURI);
		}
		context.compile();
		
		
		appendVertices();

		
		return builder.toString();
	}
	
	
	private void appendVertices() {
		
		
		
		List<Term> termList = context.asList();
		
		while (data.hasRemaining()) {
			
			mark();
			byte token = data.get();

			switch (token) {
			
			case IRI:
				String iriValue = readString();
				append("<subject> IRI ").append(iriValue);
				break;
				
			case TERM:
				short termIndex = data.getShort();
				append("<subject> TERM[").append(termIndex).append("] ");
				if (termIndex >= termList.size()) {
					throw new KonigReadException("Term index out of range: " + termIndex);
				}
				Term term = termList.get(termIndex);
				String termValue = context.expandIRI(term.getKey());
				append(termValue);
				break;
				
			case QNAME:
				short qnameIndex = data.getShort();
				append("<subject> QNAME[").append(qnameIndex).append("] ");
				if (qnameIndex >= termList.size()) {
					throw new KonigReadException("QNAME index out of range: " + qnameIndex);
				}
				Term prefixTerm = termList.get(qnameIndex);
				URI prefixURI = prefixTerm.getExpandedId();
				String prefixValue = prefixURI == null ? "null" : prefixURI.stringValue();
				String localName = readString();
				append(prefixValue).append(' ').append(localName);
				break;
				
				
			case BNODE:
				
				short bnodeId = data.getShort();
				append("<subject> BNode[").append(bnodeId).append("]");
				break;
				
			default:
				invalidToken("Invalid token for subject: ", token);
			
			}
			
			short predicateCount = data.getShort();
			append("; PREDICATE-COUNT: ").append(predicateCount).append(newline);
			
			for (int i=0; i<predicateCount; i++) {
				appendPredicate();
			}
			
			int position = data.position();
			token = peekToken();
			if (token == GRAPH) {
				data.get();
				short graphSize = data.getShort();
				builder.append(position).append(" GRAPH; SIZE: ").append(graphSize).append(newline);
				appendVertices();
			}
			
		}
	}



	private void invalidToken(String text, byte token) throws KonigReadException {
		
		text =  (token < LABEL.length) ? text+LABEL[token] : text + token;
		
		throw new KonigReadException(text);
		
	}


	private void appendPredicate() {
		
		List<Term> termList = context.asList();
		
		mark();
		append("<predicate> ");
		byte token = data.get();
		switch (token) {
		
		case IRI :
			String iriValue = readString();
			append("IRI ").append(iriValue);
			break;
			
		case TERM: 
			short termIndex = data.getShort();
			if (termIndex >= termList.size()) {
				throw new KonigReadException("Term index out of range: " + termIndex);
			}
			Term term = termList.get(termIndex);
			String termValue = context.expandIRI(term.getKey());
			append("TERM[").append(termIndex).append("] ");
			append(termValue);
			break;
			
		case QNAME:
			short qnameIndex = data.getShort();
			append("QNAME[").append(qnameIndex).append("] ");
			if (qnameIndex >= termList.size()) {
				throw new KonigReadException("QNAME index out of range: " + qnameIndex);
			}
			Term prefixTerm = termList.get(qnameIndex);
			URI prefixURI = prefixTerm.getExpandedId();
			String prefixValue = prefixURI == null ? "null" : prefixURI.stringValue();
			String localName = readString();
			append(prefixValue).append(' ').append(localName);
			break;
			
		default:
			invalidToken("Invalid predicate token: ", token);
		
		}
		
		short objectCount = data.getShort();
		append("; OBJECT-COUNT: ").append(objectCount).append(newline);
		
		for (int i=0; i<objectCount; i++) {
			appendObject();
		}
		
		
		
	}


	private void appendObject() {
		
		List<Term> termList = context.asList();
		
		mark();
		append("<object> ");
		
		byte token = data.get();
		switch (token) {
		
		case RESOURCE :
			int position = data.getInt();
			append("RESOURCE; POSITION:").append(position);
			break;
			
		case BNODE :

			short bnodeId = data.getShort();
			append("BNode[").append(bnodeId).append(']');
			break;
			
		case TERM: 
			short termIndex = data.getShort();
			if (termIndex >= termList.size()) {
				throw new KonigReadException("Term index out of range: " + termIndex);
			}
			Term term = termList.get(termIndex);
			String termValue = context.expandIRI(term.getKey());
			append("TERM[").append(termIndex).append("]; VALUE: ").append(termValue);
			break;
			
		case QNAME:
			short qnameIndex = data.getShort();
			append("QNAME[").append(qnameIndex).append("] ");
			if (qnameIndex >= termList.size()) {
				throw new KonigReadException("QNAME index out of range: " + qnameIndex);
			}
			Term prefixTerm = termList.get(qnameIndex);
			URI prefixURI = prefixTerm.getExpandedId();
			String prefixValue = prefixURI == null ? "null" : prefixURI.stringValue();
			String localName = readString();
			append(prefixValue).append("; VALUE: ").append(localName);
			break;
			
		case PLAIN:
			append("PLAIN; VALUE: ").append(readString());
			break;
			
		case LITERAL_TERM :
			termIndex = data.getShort();
			if (termIndex >= termList.size()) {
				throw new KonigReadException("Term index out of range: " + termIndex);
			}
			term = termList.get(termIndex);
			termValue = context.expandIRI(term.getKey());
			append("TYPE_TERM[").append(termIndex).append("] ").append(termValue);
			append("; VALUE: ").append(readString());
			break;
			
		case LITERAL_QNAME:
			qnameIndex = data.getShort();
			append("TYPE_QNAME[").append(qnameIndex).append("] ");
			if (qnameIndex >= termList.size()) {
				throw new KonigReadException("QNAME index out of range: " + qnameIndex);
			}
			prefixTerm = termList.get(qnameIndex);
			prefixURI = prefixTerm.getExpandedId();
			prefixValue = prefixURI == null ? "null" : prefixURI.stringValue();
			localName = readString();
			append(prefixValue).append(' ').append(localName);
			append("; VALUE: ").append(readString());
			break;
			
		case LITERAL_IRI:
			append("TYPE_IRI ").append(readString()).append("; VALUE: ").append(readString());
			break;
			
		case LANG:
			append("LANG ").append(readString()).append("; VALUE: ").append(readString());
			break;
			
		default:
			invalidToken("Invalid object token: ", token);
			
		}
		builder.append(newline);
		
	}


	private StringBuilder append(String text) {
		return builder.append(text);
	}
	
	private StringBuilder mark() {
		return builder.append(data.position()).append(' ');		
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

	private byte peekToken() {
		if (!data.hasRemaining()) {
			return 0;
		}
		int mark = data.position();
		byte token = data.get();
		data.position(mark);
		return token;
	}
}
