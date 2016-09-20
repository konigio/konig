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
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * A Turtle writer that uses compact BNode notation.
 * @author Greg McFall
 *
 */
public class CompactTurtleWriter extends TurtleWriter {
	
	private List<Context> stack = new ArrayList<>();

	public CompactTurtleWriter(OutputStream out) {
		super(out);
		stack.add(new Context());
	}
	
	public CompactTurtleWriter(Writer writer) {
		super(writer);
		stack.add(new Context());
	}
	
	@Override
    public void handleStatement(Statement st) throws RDFHandlerException {
		
		Resource subject = st.getSubject();
		URI predicate = st.getPredicate();
		Value object = st.getObject();
		
		Context context = peek();
		
		try {
			
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
				writer.write(" [ ");
				writer.increaseIndentation();
				writer.writeEOL();
				
				Context next = new Context();
				next.lastSubject = lastWrittenSubject = (BNode) object;
				
				stack.add(next);
			} else {
				writeValue(object);
			}
			
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}
	
	protected void writeLiteral(Literal lit)
			throws IOException {
		URI type = lit.getDatatype();
		if (XMLSchema.INT.equals(type)) {
			writer.write(lit.getLabel());
		} else {
			super.writeLiteral(lit);
		}
	}
	
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
		
		return stack.get(stack.size()-1);
	}



	private static class Context {
		Resource lastSubject;
		URI lastPredicate;
	}

}
