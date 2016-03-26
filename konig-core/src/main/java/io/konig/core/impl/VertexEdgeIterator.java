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
	
	private Edge current;
	
	public VertexEdgeIterator(Iterator<Entry<URI, Set<Edge>>> outer) {
		this.outer = outer;
		while (outer.hasNext()) {
			inner = outer.next().getValue().iterator();
		}
		doNext();
	}

	@Override
	public boolean hasNext() {
		return current!=null;
	}

	@Override
	public Edge next() {
		Edge result = current;
		doNext();
		return result;
	}

	private void doNext() {
		current = null;
		if (inner == null) {
			return;
		}
		if (inner.hasNext()) {
			current = inner.next();
		} else {
			while (outer.hasNext()) {
				inner = outer.next().getValue().iterator();
				if (inner.hasNext()) {
					current = inner.next();
					break;
				}
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
		
	}

}
