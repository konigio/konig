package io.konig.core;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

public class NameMap extends HashMap<String,URI> {
	private static final long serialVersionUID = 1L;
	public static URI AMBIGUOUS = new URIImpl("urn:ambiguous");
	
	public void addAll(Graph graph) {
		for (Edge edge : graph) {
			Resource subject = edge.getSubject();
			URI predicate = edge.getPredicate();
			Value object = edge.getObject();
			
			add(subject);
			add(predicate);
			add(object);
		}
	}

	public void add(Value value) {
		if (value instanceof URI) {
			URI uri = (URI) value;
			String localName = uri.getLocalName();
			put(localName, uri);
		}
	}
	
	public URI put(String name, URI uri) {
		URI result = super.put(name, uri);
		if (result != null) {
			if (result != AMBIGUOUS) {
				super.put(name, AMBIGUOUS);
			}
			result = AMBIGUOUS;
		}
		
		return result;
	}
	
	public URI get(String name) {
		URI result = super.get(name);
		if (result == AMBIGUOUS) {
			result = null;
		}
		return result;
	}

}
