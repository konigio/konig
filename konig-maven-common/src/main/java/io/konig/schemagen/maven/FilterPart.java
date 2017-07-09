package io.konig.schemagen.maven;

import java.util.Set;

abstract public class FilterPart {
	
	private Set<String> namespaces;
	
	public Set<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

	abstract public boolean acceptNamespace(String namespace);

}
