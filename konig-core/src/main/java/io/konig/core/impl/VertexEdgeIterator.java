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
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Edge;

public class VertexEdgeIterator implements Iterator<Edge> {
	
	private Iterator<Entry<URI, Set<Edge>>> outer;
	private Iterator<Edge> inner;
	
	
	public VertexEdgeIterator(Iterator<Entry<URI, Set<Edge>>> outer) {
		this.outer = outer;
	}
	@Override
	public boolean hasNext() {
		
		if (inner == null) {
			while (outer.hasNext()) {
				Entry<URI, Set<Edge>> e = outer.next();
				Set<Edge> set = e.getValue();
				inner = set.iterator();
				if (inner.hasNext()) {
					return true;
				}
			}
		} else {
			
			if (inner.hasNext()) {
				return true;
			}
			
			while (outer.hasNext()) {
				Entry<URI, Set<Edge>> e = outer.next();
				Set<Edge> set = e.getValue();
				inner = set.iterator();
				if (inner.hasNext()) {
					return true;
				}
			}
		}
		
		
		return false;
	}
	@Override
	public Edge next() {
		return inner.next();
	}
	@Override
	public void remove() {
		throw new RuntimeException("Operation not supported");
	}
	
}
