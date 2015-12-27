package io.konig.core;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class GraphBuilder {
	
	private Graph graph;
	private ValueFactory valueFactory;

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
	
	
	

}
