package io.konig.core.delta;

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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;

public class PlainTextChangeSetReportWriter implements ChangeSetReportWriter {
	private static final String ADD             = "+    ";
	private static final String REMOVE          = "-    ";
	private static final String KEYVALUE        = " *   ";
	private static final String KEYTERM         = " #   ";
	private static final String ADD_KEYVALUE    = "+*   ";
	private static final String REMOVE_KEYVALUE = "-*   ";
	private static final String ADD_KEYTERM     = "+#   ";
	private static final String REMOVE_KEYTERM  = "-#   ";
	private static final String NONE            = "?    ";
	
	private NamespaceManager nsManager;
	private int indentSize = 4;
	
	

	public PlainTextChangeSetReportWriter(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}



	@Override
	public void write(Graph changeSet, Writer writer) throws IOException {
		
		Worker worker = new Worker(changeSet, writer);
		worker.run();
	}
	
	private class Worker {
		Graph graph;
		PrintWriter writer;
		List<Context> stack = new ArrayList<>();
		
		public Worker(Graph graph, Writer writer) {
			this.graph = graph;
			this.writer = writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer);
		}

		public void run() {
			
			writeAll(graph, false);
			if (graph.size() > 0) {
				writer.println(" .");
			} else {
				writer.println();
			}
			writer.flush();
			
		}
		
		private void writeAll(Collection<Edge> collection, boolean includeBNode) {
			for (Edge e : collection) {
				
				if (includeBNode || (e.getSubject() instanceof URI)) {
					handleStatement(e);
					
					if (e.getObject() instanceof BNode) {
						BNode bnode = (BNode) e.getObject();

						Vertex vertex = graph.getVertex(bnode);
						Set<Edge> set = vertex.outEdgeSet();
						writeAll(set, true);
					}
				}
				
			}
			
		}

		

		private void handleStatement(Edge edge) {
			Context context = peek();
			if (context == null) {
				context = new Context();
				stack.add(context);
			}

			Resource subject = edge.getSubject();
			URI predicate = edge.getPredicate();
			Value object = edge.getObject();
			
			context = closeStatement(context, edge);
			
			String function = functionString(edge);
			writer.print(function);
			indent();

			if (context.lastSubject==null && subject instanceof BNode) {
				context.lastSubject = subject;
			}
			
			if (subject.equals(context.lastSubject)) {
				tab();
			} else {
				context.lastSubject = subject;
				context.lastPredicate = null;
				String id = resourceId(subject);
				writer.print(id);
			}
			
			if (predicate.equals(context.lastPredicate)) {
				tab();
			} else {
				context.lastPredicate = predicate;
				String text = resourceId(predicate);
				writer.print(' ');
				writer.print(text);
				writer.print(' ');
			}

			String value = value(object);
			writer.print(value);
			
			if (object instanceof BNode) {
				Context next = new Context();
//				next.lastSubject = (BNode) object;
				stack.add(next);
			}
		}

		
		private Context closeStatement(Context context, Edge edge) {
			if (edge.getSubject().equals(context.lastSubject)) {
				if (edge.getPredicate().equals(context.lastPredicate)) {
					writer.println(" ,");
				} else {
					
					writer.println(" ;");
				}
			} else {
				
				if (context.lastSubject instanceof BNode) {
					stack.remove(stack.size()-1);
					context = peek();
					writer.print(']');
					context = closeStatement(context, edge);
				} else if (context.lastSubject instanceof URI) {
					writer.println(" .");
				} else {
					writer.println();
					
				}
			}
			
			return context;
			
		}

		private String value(Value object) {
			if (object instanceof Resource) {
				return resourceId((Resource)object);
			}
			
			Literal literal = (Literal)object;
			String text = literal.stringValue();
			String lang = literal.getLanguage();
			if (lang != null) {
				StringBuilder builder = new StringBuilder();
				builder.append('"');
				builder.append(text);
				builder.append("\"@");
				builder.append(lang);
				return builder.toString();
			}
			if (text.equals("true") || text.equals("false")) {
				return text;
			}
			
			try {
				Double.parseDouble(text);
				return text;
			} catch (Throwable oops) {

				StringBuilder builder = new StringBuilder();
				builder.append('"');
				builder.append(text);
				builder.append('"');
				return builder.toString();
			}
		}

		private String resourceId(Resource node) {
			StringBuilder builder = new StringBuilder();
			if (node instanceof BNode) {
				return "[";
			} else {
				URI uri = (URI) node;
				String namespace = uri.getNamespace();
				Namespace ns = nsManager.findByName(namespace);
				if (ns != null) {
					builder.append(ns.getPrefix());
					builder.append(':');
					builder.append(uri.getLocalName());
				} else {
					builder.append('<');
					builder.append(node.stringValue());
					builder.append('>');
				}
				
			}
			return builder.toString();
		}

		private void tab() {
			for (int i=0; i<indentSize; i++) {
				writer.print(' ');
			}
			
		}

		private Context peek() {
			return stack.isEmpty() ? null : stack.get(stack.size()-1);
		}

		private void indent() {
			int width = (stack.size()-1)*indentSize;
			for (int i=0; i<width; i++) {
				writer.print(' ');
			}
			
		}

		private String functionString(Edge edge) {
			Value value = edge.getAnnotation(RDF.TYPE);
			String result = 
				Konig.Dictum.equals(value)      ? ADD :
				Konig.Falsity.equals(value)   ? REMOVE :
				Konig.KeyValue.equals(value) ? KEYVALUE :
				match(edge, Konig.Falsity, Konig.KeyValue, value) ? REMOVE_KEYVALUE :
				match(edge, Konig.Dictum,  Konig.KeyValue, value) ? ADD_KEYVALUE :
				match(edge, Konig.Falsity, Konig.KeyTerm, value)  ? REMOVE_KEYTERM :
				match(edge, Konig.Dictum,  Konig.KeyTerm, value)  ? ADD_KEYTERM :
				NONE;
			return result;
		}
		
		private boolean match(Edge e, URI a, URI b, Value value) {
			return e.matches(a, value) && e.matches(b, value);
		}
		
		
	}
	
	private static class Context {
		Resource lastSubject;
		URI lastPredicate;
	}

	@Override
	public void write(Graph changeSet, OutputStream out) throws IOException {
		write(changeSet, new OutputStreamWriter(out));
		
	}

}
