package io.konig.core;

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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

public class DepthFirstEdgeIterable implements Iterable<Edge> {
	private Graph graph;
	private Edge current = null;
	private List<Iterator<Edge>> stack = new ArrayList<>();
	
	public DepthFirstEdgeIterable(Graph graph) {
		this.graph = graph;
	}
	
	private Iterator<Edge> peek() {
		return stack.isEmpty() ? null : stack.get(stack.size()-1);
	}
	
	private Iterator<Edge> pop() {
		return stack.isEmpty() ? null : stack.remove(stack.size()-1);
	}

	@Override
	public Iterator<Edge> iterator() {
		stack.add(graph.iterator());
		return new EdgeIterator();
	}
	
	class EdgeIterator implements Iterator<Edge> {
		
		
		public EdgeIterator() {
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
			
			while (current==null && !stack.isEmpty()) {
				Iterator<Edge> sequence = peek();
				if (sequence.hasNext()) {
					current = sequence.next();
					Value object = current.getObject();
					Resource subject = current.getSubject();
					if (subject instanceof BNode && stack.size()==1) {
						current = null;
					} else if (object instanceof BNode){
					
						BNode bnode = (BNode) object;
						Vertex v = graph.getVertex(bnode);
						Set<Edge> set = v.outEdgeSet();
						stack.add(set.iterator());
						
					}
				} else {
					pop();
				}
			}
			
			
			
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
	}

}
