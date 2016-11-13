package io.konig.core.impl;

import java.util.ArrayList;

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


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Traversal;
import io.konig.core.Vertex;

public class VertexImpl implements Vertex {
	private transient Graph graph;
	private Graph namedGraph;
	private Resource id;
	
	private EdgeMapImpl out = new EdgeMapImpl();
	private EdgeMapImpl in = new EdgeMapImpl();
	
	private static Set<Edge> emptySet = new HashSet<Edge>();
	

	public VertexImpl(Graph graph, Resource id) {
		this.graph = graph;
		this.id = id instanceof URI ? 
			new URIVertex(id.stringValue(), this) : 
			new BNodeVertex(id.stringValue(), this);
	}

	public Resource getId() {
		return id;
	}

	public Graph getGraph() {
		return graph;
	}
	
	public void add(Edge e) {
		if (e.getSubject().equals(id)) {
			out.add(e);
		}
		if (e.getObject().equals(id)) {
			in.add(e);
		}
	}

	public Set<Edge> outProperty(URI predicate) {
		Set<Edge> result = out.get(predicate);
		return result == null ? emptySet : result;
	}

	public Set<Edge> inProperty(URI predicate) {
		Set<Edge> result = in.get(predicate);
		return result == null ? emptySet : result;
	}

	@Override
	public Set<Entry<URI, Set<Edge>>> inEdges() {
		return in.entries();
	}


	public Set<Entry<URI, Set<Edge>>> outEdges() {
		return out.entries();
	}

	public Traversal asTraversal() {
		return new TraversalImpl(this);
	}

	public void remove(Edge edge) {
		
		if (edge.getSubject().equals(id)) {
			Set<Edge> out = outProperty(edge.getPredicate());
			out.remove(edge);
		}
		if (edge.getObject().equals(id)) {
			Set<Edge> in = inProperty(edge.getPredicate());
			in.remove(edge);
		}
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(id.stringValue());
		builder.append('\n');
		appendProperties(builder, this, 1);
		
		return builder.toString();
	}
	
	private void appendValue(StringBuilder builder, Value value, int depth) {
	
		if (value instanceof BNode) {
			appendBNode(builder, (BNode)value, depth);
		} else {
			builder.append(value.stringValue());
			builder.append('\n');
		}
	}
	
	private void appendBNode(StringBuilder builder, BNode value, int depth) {
		Vertex node = graph.getVertex(value);
		builder.append("[ <");
		builder.append(value.getID());
		builder.append(">\n");
//		builder.append("[\n");
		depth++;
		appendProperties(builder, node, depth);
		depth--;
		indent(builder, depth);
		builder.append("]\n");
	}

	private void appendProperties(StringBuilder builder, Vertex node, int depth) {
		if (node == null) {
			return;
		}
		Set<Entry<URI,Set<Edge>>> out = node.outEdges();
		for (Entry<URI,Set<Edge>> entry : out) {
			URI predicate = entry.getKey();
			Set<Edge> edges = entry.getValue();
			indent(builder, depth);
			int size = edges.size();
			if (size > 0) {
				builder.append(predicate.getLocalName());
				if (size==1) {
					Value value = edges.iterator().next().getObject();
					builder.append(' ');
					appendValue(builder, value, depth);
					
				} else {
					builder.append('\n');
					depth++;
					for (Edge e : edges) {
						Value value = e.getObject();
						indent(builder, depth);
						appendValue(builder, value, depth);
					}
					depth--;
				}
			}
		}
		
	}

	private void indent(StringBuilder builder, int depth) {
		for (int i=0; i<depth; i++) {
			builder.append("  ");
		}
		
	}

	@Override
	public Graph asNamedGraph() {
		return namedGraph;
	}

	@Override
	public Graph assertNamedGraph() {
		if (namedGraph == null) {
			namedGraph = new MemoryGraph();
			namedGraph.setId(id);
		}
		return namedGraph;
	}

	@Override
	public boolean hasEdge(Edge edge) {
		
		Set<Edge> set = edge.getSubject().equals(id) ? 
			outProperty(edge.getPredicate()) :
			inProperty(edge.getPredicate());
		
		return set.contains(edge);
	}

	@Override
	public Set<Edge> outEdgeSet() {
		return new OutEdgeSet(this, this.outEdges());
	}

	@Override
	public Set<Edge> inEdgeSet() {
		return new InEdgeSet(this, this.in.entries());
	}

	@Override
	public List<Value> asList() {
		
		List<Value> result = null;
		
		Vertex v = this;
		while (v != null) {
			Vertex w = v;
			v = null;
			Set<Edge> first = w.outProperty(RDF.FIRST);
			if (first != null) {
				Iterator<Edge> sequence = first.iterator();
				if (sequence.hasNext()) {
					Edge firstEdge = sequence.next();
					Value object = firstEdge.getObject();
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(object);
					
					Set<Edge> rest = w.outProperty(RDF.REST);
					if (rest != null) {
						sequence = rest.iterator();
						if (sequence.hasNext()) {
							Edge restEdge = sequence.next();
							object = restEdge.getObject();
							if (!RDF.NIL.equals(object) && object instanceof Resource) {
								v = graph.getVertex((Resource) object);
							}
						}
					}
					
				}
			}
		}
		
		if (result == null && (RDF.NIL.equals(id) || hasProperty(RDF.TYPE, RDF.LIST))) {
			result = new ArrayList<>();
		}
		
		return result;
	}

	@Override
	public Value getValue(URI predicate) throws KonigException {
		Set<Edge> set = outProperty(predicate);
		if (set==null || set.isEmpty()) {
			return null;
		}
		Iterator<Edge> sequence = set.iterator();
		return sequence.next().getObject();
	}

	@Override
	public boolean hasProperty(URI predicate, Value value) {
		Set<Edge> set = outProperty(predicate);
		for (Edge e : set) {
			if (value.equals(e.getObject())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void addProperty(URI property, Value value) {
		graph.add(new EdgeImpl(id, property, value));
	}

	@Override
	public Integer integerValue(URI predicate) {
		Value value = getValue(predicate);
		
		return value == null ? null : Integer.parseInt(value.stringValue());
	}

	@Override
	public String stringValue(URI predicate) {
		Value value = getValue(predicate);
		return value==null ? null : value.stringValue();
	}

	@Override
	public Double doubleValue(URI predicate) {
		Value value = getValue(predicate);
		
		return value==null ? null : Double.parseDouble(value.stringValue());
	}

	@Override
	public Vertex vertexValue(URI predicate) {
		Value value = getValue(predicate);
		return value instanceof Resource ? graph.vertex((Resource)value) : null;
	}

	@Override
	public boolean isOrphan() {
		Set<Entry<URI,Set<Edge>>> set = inEdges();
		for (Entry<URI,Set<Edge>> entry : set) {
			Set<Edge> s = entry.getValue();
			if (!s.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Vertex getVertex(URI predicate) throws KonigException {
		Value value = getValue(predicate);
		
		return (value instanceof Resource) ? graph.getVertex((Resource) value) : null;
	}

	@Override
	public URI getURI(URI predicate) {

		Set<Edge> set = outProperty(predicate);
		if (set==null || set.isEmpty()) {
			return null;
		}
		for (Edge edge : set) {
			Value value = edge.getObject();
			if (value instanceof URI) {
				return (URI) value;
			}
		}
		return null;
	}

	@Override
	public Set<Value> getValueSet(URI predicate) {
		
		Set<Edge> edgeSet = outProperty(predicate);
		Set<Value> result = new LinkedHashSet<>();
		
		for (Edge e : edgeSet) {
			result.add(e.getObject());
		}
		return result;
	}

}
