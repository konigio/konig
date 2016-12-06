package io.konig.runtime.io;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.openrdf.model.URI;

public class TypeSet extends LinkedHashSet<URI>{
	private static final long serialVersionUID = 1L;
	
	public TypeSet append(URI value) {
		add(value);
		return this;
	}
	
	public TypeSet appendAll(Collection<URI> collection) {
		addAll(collection);
		return this;
	}

	

}
