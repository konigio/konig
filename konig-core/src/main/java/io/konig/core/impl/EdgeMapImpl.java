package io.konig.core.impl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Edge;

public class EdgeMapImpl {

	private Map<URI, Set<Edge>> map = new LinkedHashMap<URI, Set<Edge>>();
	
	public void add(Edge edge) {
		Set<Edge> set = map.get(edge.getPredicate());
		if (set == null) {
			set = new HashSet<Edge>();
			map.put(edge.getPredicate(), set);
		}
		set.add(edge);
	}
	
	public Set<Edge> get(URI predicate) {
		return map.get(predicate);
	}
	
	public Set<Entry<URI, Set<Edge>>> entries() {
		return map.entrySet();
	}
}
