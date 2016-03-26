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


import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Vertex;

public class OutEdgeSet implements Set<Edge> {
	Vertex vertex;
	Set<Entry<URI, Set<Edge>>> edgeSet;
	
	

	public OutEdgeSet(Vertex vertex, Set<Entry<URI, Set<Edge>>> outSet) {
		this.vertex = vertex;
		this.edgeSet = outSet;
	}

	@Override
	public boolean add(Edge e) {
		return vertex.getGraph().add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Edge> c) {
		
		return vertex.getGraph().addAll(c);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public boolean contains(Object o) {
		
		return vertex.getGraph().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		return vertex.getGraph().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return edgeSet.isEmpty();
	}

	@Override
	public Iterator<Edge> iterator() {
		return new VertexEdgeIterator(vertex.outEdges().iterator());
	}

	@Override
	public boolean remove(Object o) {
		return vertex.getGraph().remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return vertex.getGraph().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return vertex.getGraph().retainAll(c);
	}

	@Override
	public int size() {
		int count = 0;
		for (Entry<URI, Set<Edge>> e : edgeSet) {
			Set<Edge> s = e.getValue();
			count += s.size();
		}
		return count;
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

}
