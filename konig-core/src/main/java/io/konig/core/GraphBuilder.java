package io.konig.core;

import java.util.LinkedList;

import org.openrdf.model.BNode;

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


import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.impl.KonigLiteral;

public class GraphBuilder {
	
	private Graph graph;
	private ValueFactory valueFactory;
	
	private LinkedList<Resource> stack = new LinkedList<>();

	public GraphBuilder(Graph graph) {
		this.graph = graph;
		valueFactory = new ValueFactoryImpl();
	}
	
	public GraphBuilder statement(Resource subject, URI predicate, Value object) {
		graph.edge(subject, predicate, object);
		return this;
	}
	
	public GraphBuilder objectProperty(String subject, String predicate, String object) {
		graph.edge(uri(subject), uri(predicate), uri(object));
		return this;
	}
	
	public GraphBuilder literalProperty(String subject, String predicate, String object) {
		graph.edge(uri(subject), uri(predicate), literal(object));
		return this;
	}
	
	public GraphBuilder literalProperty(Resource subject, URI predicate, String object) {
		graph.edge(subject, predicate, literal(object));
		return this;
	}
	
	public Literal literal(String value) {
		return valueFactory.createLiteral(value);
	}
	
	public URI uri(String value) {
		return valueFactory.createURI(value);
	}
	
	public GraphBuilder beginSubject() {
		BNode bnode = new BNodeImpl(UidGenerator.INSTANCE.next());
		return beginSubject(bnode);
	}
	
	public GraphBuilder beginSubject(Vertex v) {
		return beginSubject(v.getId());
	}
	
	public GraphBuilder beginSubject(String iri) {
		return beginSubject(new URIImpl(iri));
	}
	
	public GraphBuilder beginBNode(URI predicate) {
		Vertex v = graph.vertex();
		addProperty(predicate, v.getId());
		return beginSubject(v.getId());
	}
	
	public GraphBuilder beginSubject(Resource subject) {
		stack.add(subject);
		return this;
	}
	
	public GraphBuilder endSubject() {
		stack.removeLast();
		return this;
	}
	
	public Resource peek() {
		return stack.getLast();
	}
	
	public Resource pop() {
		return stack.removeLast();
	}
	
	public GraphBuilder addProperty(URI predicate, Vertex object) {
		return addProperty(predicate, object.getId());
	}
	
	public GraphBuilder addProperty(URI predicate, Value object) {
		return statement(peek(), predicate, object);
	}
	
	public GraphBuilder addList(URI predicate, Value...object) {
		Resource subject = peek();
		Resource list = graph.vertex().getId();
		graph.edge(subject, predicate, list);
		for (int i=0; i<object.length; i++) {
			graph.edge(list, RDF.FIRST, object[i]);
			if (i == object.length-1) {
				graph.edge(list, RDF.REST, RDF.NIL);
			} else {
				Resource rest = graph.vertex().getId();
				graph.edge(list, RDF.REST, rest);
				list = rest;
			}
		}
		return this;
	}
	
	public GraphBuilder addProperty(URI predicate, int value) {
		return addProperty(predicate, valueFactory.createLiteral(value));
	}
	
	public GraphBuilder addLiteral(URI predicate, String object) {
		return addProperty(predicate, new KonigLiteral(object));
	}
	
	public GraphBuilder addFloat(URI predicate, float value) {
		return addProperty(predicate, new LiteralImpl(Float.toString(value), XMLSchema.FLOAT));
	}
	
	public GraphBuilder addLiteral(URI predicate, String value, URI datatype) {
		return addProperty(predicate, new KonigLiteral(value, datatype));
	}
	
	
	

}
