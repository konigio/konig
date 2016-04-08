package io.konig.core.impl;

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


import java.util.Iterator;
import java.util.Set;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;

public class EdgeIterator implements Iterator<Edge> {
	
	private Iterator<Vertex> vertexIterator;
	private Iterator<Edge> edgeIterator;
	
	public EdgeIterator(Graph graph) {
		vertexIterator = graph.vertices().iterator();
	}

	@Override
	public boolean hasNext() {
		
		if (edgeIterator == null) {
			while (vertexIterator.hasNext()) {
				Vertex v = vertexIterator.next();
				edgeIterator = v.outEdgeSet().iterator();
				if (edgeIterator.hasNext()) {
					return true;
				}
			}
		} else {
			
			if (edgeIterator.hasNext()) {
				return true;
			}
			
			while (vertexIterator.hasNext()) {
				Vertex v = vertexIterator.next();
				Set<Edge> set = v.outEdgeSet();
				edgeIterator = set.iterator();
				if (edgeIterator.hasNext()) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public Edge next() {
		return edgeIterator.next();
	}

	@Override
	public void remove() {
		edgeIterator.remove();
	}

}
