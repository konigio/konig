package io.konig.core;

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
