package io.konig.core.impl;

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
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Graph;
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
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(id.stringValue());
		buffer.append('\n');
		Set<Entry<URI,Set<Edge>>> out = outEdges();
		for (Entry<URI,Set<Edge>> entry : out) {
			URI predicate = entry.getKey();
			Set<Edge> edges = entry.getValue();
			buffer.append("  ");
			buffer.append(predicate.stringValue());
			if (edges.size()==1) {
				buffer.append("  ");
				String value = edges.iterator().next().getObject().stringValue();
				buffer.append(value);
				buffer.append('\n');
			} else {
				buffer.append('\n');
				for (Edge e : edges) {
					String value = e.getObject().stringValue();
					buffer.append("    ");
					buffer.append(value);
					buffer.append('\n');
				}
			}
		}
		
		
		return buffer.toString();
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


}
