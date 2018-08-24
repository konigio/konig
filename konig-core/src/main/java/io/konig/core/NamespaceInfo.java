package io.konig.core;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

/**
 * A set that holds all the terms for a given Namespace.
 * @author Greg McFall
 *
 */
public class NamespaceInfo {
	
	private String namespaceIri;
	private Set<URI> type = new HashSet<>();
	private Set<URI> terms = new HashSet<>();
	
	
	public NamespaceInfo(String namespaceIri) {
		this.namespaceIri = namespaceIri;
	}
	
	public Set<URI> getType() {
		return type;
	}
	
	public Set<URI> getTerms() {
		return terms;
	}


	public String getNamespaceIri() {
		return namespaceIri;
	}
	
	public String toString() {
		return "NamespaceInfo[" + namespaceIri + "]";
	}

}
