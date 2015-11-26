package io.konig.core.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;

import io.konig.core.NamespaceManager;

public class MemoryNamespaceManager implements NamespaceManager {
	private Map<String, Namespace> map = new HashMap<String, Namespace>();

	public Namespace findByPrefix(String prefix) {
		return map.get(prefix);
	}

	public Namespace findByName(String name) {
		return map.get(name);
	}

	public NamespaceManager add(Namespace ns) {
		map.put(ns.getPrefix(), ns);
		map.put(ns.getName(), ns);
		return this;
	}

	public NamespaceManager add(String prefix, String namespace) {
		return add(new NamespaceImpl(prefix, namespace));
	}

}
