package io.konig.core.io;

/*
 * #%L
 * konig-core
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


import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.turtle.TurtleUtil;
import org.openrdf.rio.turtle.TurtleWriter;

import info.aduna.io.IndentingWriter;
import info.aduna.text.StringUtil;

/**
 * A Turtle writer that uses compact BNode notation.
 * @author Greg McFall
 *
 */
public class CompactTurtleWriter extends TurtleWriter {
	
	private List<Context> stack = new ArrayList<>();
	private String baseIRI = null;
	
	

	public String getBaseIRI() {
		return baseIRI;
	}

	public void setBaseIRI(String baseIRI) {
		this.baseIRI = baseIRI;
	}

	public CompactTurtleWriter(OutputStream out) {
		super(out);
		stack.add(new Context());
	}
	
	public CompactTurtleWriter(Writer writer) {
		super(writer);
		stack.add(new Context());
	}
	
	protected void writeURI(URI uri)
			throws IOException
		{
			String uriString = uri.toString();
			String namespaceName = uri.getNamespace();
			String localName = uri.getLocalName();

			// Try to find a prefix for the URI's namespace

			String prefix = namespaceTable.get(namespaceName);
			if (prefix == null) {
				prefix = namespaceTable.get(uriString);
				if (prefix != null) {
					localName = "";
				}
			}
			

			if (useCurie(prefix, localName)) {
				// Namespace is mapped to a prefix; write abbreviated URI
				writer.write(prefix);
				writer.write(":");
				writer.write(localName);
			} else if (baseIRI!=null && uriString.startsWith(baseIRI)) {
				writer.write('<');
				writer.write(TurtleUtil.encodeURIString(uriString.substring(baseIRI.length())));
				writer.write('>');
			}
			else {
				
				// Write full URI
				writer.write("<");
				writer.write(TurtleUtil.encodeURIString(uriString));
				writer.write(">");
			}
		}
	
	private boolean useCurie(String prefix, String localName) {
		if (prefix != null && localName !=null) {
			if (localName.length()==0) {
				return true;
			}
			char c = localName.charAt(0);
			if (('a'<=c && c<='z') || ('A'<=c && c<='Z')) {
				for (int i=1; i<localName.length(); i++) {
					if (
						!('a'<=c && c<='z') && 
						!('A'<=c && c<='Z') &&
						!('0'<=c && c<='9') &&
						(c!='-') &&
						(c!='_')
					) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
    public void handleStatement(Statement st) throws RDFHandlerException {
		
		Resource subject = st.getSubject();
		URI predicate = st.getPredicate();
		Value object = st.getObject();
		
		Context context = peek();
		
		try {
			
			
			if (RDF.FIRST.equals(predicate) || RDF.REST.equals(predicate)) {
				ListInfo listInfo = context.listInfo;
				boolean startList = false;
				if (listInfo == null) {
					writer.write("(");
					context.listInfo = listInfo = new ListInfo();
					startList = true;
					
				}  
				if (RDF.FIRST.equals(predicate)) {
					listInfo.first = object;
					if (!startList) {
						writer.write(" ");
					}
					if (object instanceof BNode) {
						writer.write(" [ ");
						writer.increaseIndentation();
						writer.writeEOL();
						
						Context next = new Context();
						next.lastSubject = lastWrittenSubject = (BNode) object;
						
						stack.add(next);
					} else {
						writeValue(object);
					}
				} else if (RDF.REST.equals(predicate)) {
					
					listInfo.rest = (Resource) object;
				}
				
				if (listInfo.first!=null && listInfo.rest!=null && RDF.NIL.equals(listInfo.rest)) {
					writer.write(")");
					context.listInfo = null;
					stack.remove(stack.size()-1);
				}
				return;
			}
			
			if (context.bracket != null) {

				writer.write(context.bracket);
				writer.increaseIndentation();
				writer.writeEOL();
				context.bracket = null;
			}
			
			if (context.lastSubject instanceof BNode  && !subject.equals(context.lastSubject)) {
				context = closeBNode(st);
			}

			if (subject.equals(context.lastSubject)) {
				if (predicate.equals(context.lastPredicate)) {
					writer.write(" , ");
				} else {
					if (context.lastPredicate != null) {
						writer.write(" ; ");
						writer.writeEOL();
					}
					writePredicate(predicate);
					writer.write(' ');
					context.lastPredicate = lastWrittenPredicate = predicate;
				}
			} else {
				// New subject
				if (stack.size() == 1 && context.lastSubject!=null) {
					writer.write(" . ");
					writer.writeEOL();
					writer.decreaseIndentation();
				}
				
				// Write new subject
				writer.writeEOL();
				writeResource(subject);
				writer.write(' ');
				context.lastSubject = lastWrittenSubject = subject;
				
				// Write new predicate
				writePredicate(predicate);
				writer.write(' ');
				context.lastPredicate = lastWrittenPredicate = predicate;
				writer.increaseIndentation();
			}
			
			if (object instanceof BNode) {
				
				Context next = new Context();
				next.bracket = " [ ";
				next.lastSubject = lastWrittenSubject = (BNode) object;
				
				stack.add(next);
			} else {
				writeValue(object);
			}
			
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	protected void writeResource(Resource res)
		throws IOException
	{
		if (res instanceof URI) {
			writeURI((URI)res);
		}
		else {
			writer.write("[]");
		}
	}
	
	protected void writeLiteral(Literal lit)
			throws IOException {
		URI type = lit.getDatatype();
		if (XMLSchema.INT.equals(type)) {
			writer.write(lit.getLabel());
		} else {

			String label = lit.getLabel();
			URI datatype = lit.getDatatype();
			if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
					|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype))
			{
				try {
					writer.write(XMLDatatypeUtil.normalize(label, datatype));
					return; // done
				}
				catch (IllegalArgumentException e) {
					// not a valid numeric typed literal. ignore error and write as
					// quoted string instead.
				}
			}
			if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
				// Write label as long string
				 writeLongString(writer, label);
			}
			else {
				// Write label as normal string
				writer.write("\"");
				encodeString(label);
				writer.write("\"");
			}

			if (Literals.isLanguageLiteral(lit)) {
				// Append the literal's language
				writer.write("@");
				writer.write(lit.getLanguage());
			}
			else if (!XMLSchema.STRING.equals(datatype) && datatype!=null) {
				// Append the literal's datatype (possibly written as an abbreviated
				// URI)
				writer.write("^^");
				writeURI(datatype);
			}
		}
	}
	
	private void encodeString(String label) throws IOException {

		for (int i=0; i<label.length();) {
			int c = label.codePointAt(i);
			switch (c) {
			case '\\' :
				writer.write("\\\\");
				break;
				
			case '\t' :
				writer.write("\\t");
				break;
				
			case '\n' :
				writer.write("\\n");
				break;
				
			case '\r' :
				writer.write("\\r");
				break;
				
			case '\"' :
				writer.write("\\\"");
				break;
				
			default :
				if ((c < 32 || c>255) && c<0xffff) {
					
					String hexValue = Integer.toHexString(c);
					
					writer.write("\\u");
					for (int k=hexValue.length(); k<4; k++) {
						writer.write('0');
					}
					writer.write(hexValue);
				} else if (c>=0xffff) {

					String hexValue = Integer.toHexString(c);
					
					writer.write("\\U");
					for (int k=hexValue.length(); k<8; k++) {
						writer.write('0');
					}
					writer.write(hexValue);
				} else {
					writer.write((char)c);
				}
				break;
					
			}
			
			
			i += Character.charCount(c);
		}
		
	}

	private void writeLongString(IndentingWriter writer, String s) throws IOException {
		
		boolean singleQuote = s.indexOf('\'')==-1;
		
		if (singleQuote) {
			writer.write("'''");
			encodeLongString(s, '\'', "\\'");
			writer.write("'''");
			
		} else {
			writer.write("\"\"\"");
			encodeLongString(s, '"', "\\\"");
			writer.write("\"\"\"");
		}
		
			
	}

	private void encodeLongString(String label, char quoteChar, String quoteString) throws IOException {
		
		for (int i=0; i<label.length();) {
			int c = label.codePointAt(i);
			
			if (c == quoteChar || c=='\n' || c=='\r') {
				writer.write(c);
				
			} else if ((c < 32 || c>255) && c<0xffff) {
				
				String hexValue = Integer.toHexString(c);
				
				writer.write("\\u");
				for (int k=hexValue.length(); k<4; k++) {
					writer.write('0');
				}
				writer.write(hexValue);
			} else if (c>=0xffff) {
	
				String hexValue = Integer.toHexString(c);
				
				writer.write("\\U");
				for (int k=hexValue.length(); k<8; k++) {
					writer.write('0');
				}
				writer.write(hexValue);
			} else {
				writer.write((char)c);
			}
			
			
			i += Character.charCount(c);
		}
		
	}

/*	
	protected void writeLiteral(Literal lit)
			throws IOException
		{
			String label = lit.getLabel();
			URI datatype = lit.getDatatype();

			if (getWriterConfig().get(BasicWriterSettings.PRETTY_PRINT)) {
				if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
						|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype))
				{
					try {
						writer.write(XMLDatatypeUtil.normalize(label, datatype));
						return; // done
					}
					catch (IllegalArgumentException e) {
						// not a valid numeric typed literal. ignore error and write as
						// quoted string instead.
					}
				}
			}

			if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
				// Write label as long string
				writer.write("\"\"\"");
				writer.write(TurtleUtil.encodeLongString(label));
				writer.write("\"\"\"");
			}
			else {
				// Write label as normal string
				writer.write("\"");
				writer.write(TurtleUtil.encodeString(label));
				writer.write("\"");
			}

			if (Literals.isLanguageLiteral(lit)) {
				// Append the literal's language
				writer.write("@");
				writer.write(lit.getLanguage());
			}
			else if (!XMLSchema.STRING.equals(datatype) || !xsdStringToPlainLiteral()) {
				// Append the literal's datatype (possibly written as an abbreviated
				// URI)
				writer.write("^^");
				writeURI(datatype);
			}
		}
*/	
	private Context closeBNode(Statement s) throws IOException {
		Context context = null;
		Resource subject = s.getSubject();
		boolean nextIsBNode = s.getObject() instanceof BNode;
		
		while (stack.size() > 1) {
			writer.decreaseIndentation();
			if (nextIsBNode) {
				writer.writeEOL();
			}
			writer.write(" ] ");
			stack.remove(stack.size()-1);
			context = peek();
			lastWrittenSubject = context.lastSubject;
			lastWrittenPredicate = context.lastPredicate;
			if (subject.equals(context.lastSubject)) {
				break;
			}
		}	
		if (context == null) {
			context = peek();
		}
		return context;
	}
	

	@Override
	public void endRDF() throws RDFHandlerException {
		try {
			while (stack.size()>1) {
				writer.write(" ] ");
				stack.remove(stack.size()-1);
			}
			writer.write(" . ");
			writer.flush();
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	private Context peek() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.get(stack.size()-1);
	}


	private static class ListInfo {
		Value first;
		Resource rest;
	}

	private static class Context {
		String bracket;
		Resource lastSubject;
		URI lastPredicate;
		ListInfo listInfo;
	}

}
