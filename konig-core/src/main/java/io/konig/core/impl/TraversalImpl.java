package io.konig.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Traversal;
import io.konig.core.Vertex;

public class TraversalImpl implements Traversal {
	
	private Graph graph;
	private List<Object> list = new ArrayList<Object>();
	
	public TraversalImpl(Graph g) {
		this.graph = g;
		
	}
	public TraversalImpl(Vertex v) {
		graph = v.getGraph();
		list.add(v);
	}

	public Traversal has(URI property) {
		TraversalImpl result = new TraversalImpl(graph);
		
		List<Object> sink = result.list;
		
		for (Object e : list) {
			
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(property);
				if (!set.isEmpty()) {
					sink.add(v);
				}
			}
		}
		
		return result;
	}

	public int size() {
		return list.size();
	}

	public Traversal hasValue(URI property, Value value) {

		TraversalImpl result = new TraversalImpl(graph);
		
		List<Object> sink = result.list;
		
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex)e;

				Set<Edge> set = v.outProperty(property);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (value.equals(object)) {
						sink.add(v);
					}
				}
			}
		}
		return result;
	}
	public Traversal addProperty(URI property, Value value) {
		
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;
				v.getGraph().edge(v.getId(), property, value);
			}
		}
		return this;
	}
	public Traversal addObject(String property, String iri) {		
		return addProperty(uri(property), uri(iri));
	}
	public Traversal addLiteral(String property, String value) {
		return addProperty(uri(property), literal(value));
	}
	
	public Literal literal(String value) {
		return new KonigLiteral(value);
	}
	
	public URI uri(String value) {
		return new URIImpl(value);
	}
	public Traversal addV(Resource... iri) {
		TraversalImpl result = new TraversalImpl(graph);
		for (int i=0; i<iri.length; i++) {
			Vertex v = graph.vertex(iri[i]);
			result.list.add(v);
		}
		return result;
	}
	public Traversal addLiteral(URI property, String value) {
		return addProperty(property, literal(value));
	}
	
	public Value firstValue(URI predicate) {
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(predicate);
				for (Edge edge : set) {
					return edge.getObject();
				}
			}
		}
		return null;
	}
	public Traversal hasValue(URI property, String value) {

		TraversalImpl result = new TraversalImpl(graph);
		
		List<Object> sink = result.list;
		
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(property);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (value.equals(object.stringValue())) {
						sink.add(v);
					}
				}
			}
		}
		return result;
	}
	public Traversal out(URI predicate) {
		TraversalImpl result = new TraversalImpl(graph);
		List<Object> sink = result.list;
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(predicate);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (object instanceof Resource) {
						Vertex outV = graph.vertex((Resource)object);
						sink.add(outV);
					} else {
						// TODO: Consider implementing a Vertex wrapper around literals
						sink.add(object);
					}
				}
			}
		}
		
		return result;
	}
	
	

}
