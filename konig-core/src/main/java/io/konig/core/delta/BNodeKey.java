package io.konig.core.delta;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;

public class BNodeKey extends BNodeImpl {
	private static final long serialVersionUID = 1L;
	private Map<String, URI> part;
	private transient BNodeKeyFactory factory;
	
	public BNodeKey(String hash, Map<String, URI> part, BNodeKeyFactory factory) {
		super(hash);
		this.part = part;
		this.factory = factory;
	}

	public String getHash() {
		return getID();
	}

	public BNodeKeyFactory getFactory() {
		return factory;
	}
	
	public URI keyPart(URI predicate) {
		return part.get(predicate.stringValue());
	}
	
	
}
