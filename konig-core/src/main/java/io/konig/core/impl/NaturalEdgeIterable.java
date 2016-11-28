package io.konig.core.impl;

/*
 * #%L
 * Konig Core
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


import java.util.Iterator;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

public class NaturalEdgeIterable implements Iterable<Edge> {
	
	private Graph graph;
	
	public NaturalEdgeIterable(Graph graph) {
		this.graph = graph;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new TopIterator(graph);
	}
	
	static class TopIterator implements Iterator<Edge> {
		private Graph graph;
		private Iterator<Vertex> vertexSequence;
		private Iterator<Edge> edgeSequence;
		private Edge current;
		
		

		public TopIterator(Graph graph) {
			this.graph = graph;
			vertexSequence = graph.vertices().iterator();
			next();
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public Edge next() {
			Edge result = current;
			current = null;
			if (edgeSequence != null) {
				current = edgeSequence.next();
				if (!edgeSequence.hasNext()) {
					edgeSequence = null;
				}
			}
			while (current==null && vertexSequence.hasNext()) {
				Vertex v = vertexSequence.next();
				Resource id = v.getId();
				
				// Skip BNodes that have incoming edges.
				if (id instanceof BNode && v.inEdgeSet().iterator().hasNext()) {
					continue;
				}
				
				Iterator<Edge> iterator = v.outEdgeSet().iterator();
				if (iterator.hasNext()) {
					BasicIterator sequence = new BasicIterator(graph, iterator);
					if (sequence.hasNext()) {
						current = sequence.next();
						edgeSequence = sequence.hasNext() ? sequence : null;
					}
				}
				
			}
			return result;
		}

		@Override
		public void remove() {
			throw new KonigException("remove method not supported");
			
		}
		
	}
	
	
	static class BasicIterator implements Iterator<Edge> {
		
		Graph graph;
		Iterator<Edge> primary;
		BasicIterator secondary;
		Edge current = null;
		

		public BasicIterator(Graph graph, Iterator<Edge> primary) {
			this.graph = graph;
			this.primary = primary;
			current = primary.next();
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public Edge next() {
			Edge result = current;
			current = null;
			
			if (secondary != null) {
				current = secondary.next();
				if (!secondary.hasNext()) {
					secondary = null;
				}
			} 

			if (current == null) {

				if (result != null && result.getObject() instanceof BNode) {
					Vertex node = graph.getVertex((BNode)result.getObject());
					Iterator<Edge> inner = node.outEdgeSet().iterator();
					if (inner.hasNext()) {
						secondary = new BasicIterator(graph, inner);
						current = secondary.next();
						if (!secondary.hasNext()) {
							secondary = null;
						}
					}
				}
				
				if (current == null && primary.hasNext()) {
					current = primary.next();
				}
				
			}
			
			return result;
		}

		@Override
		public void remove() {
			throw new KonigException("remove method not supported");
			
		}
		
	}
	
	
	
	

}
