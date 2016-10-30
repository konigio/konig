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


import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;

/**
 * A set of values that is accessed as a BNode.  This class is used to represent multi-valued
 * properties of an Edge.
 * @author Greg McFall
 *
 */
public class ValueSet extends BNodeImpl implements Set<Value>{

	private static final long serialVersionUID = 1L;
	private Set<Value> set = new LinkedHashSet<>();

	@Override
	public boolean add(Value e) {
		return set.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Value> c) {
		return set.addAll(c);
	}

	@Override
	public void clear() {
		set.clear();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<Value> iterator() {
		return set.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return set.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return retainAll(c);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		String comma = "";
		for (Value v : set) {
			builder.append(comma);
			comma = ", ";
			builder.append(v.stringValue());
		}
		builder.append(')');
		return builder.toString();
	}

}
