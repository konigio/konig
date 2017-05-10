package io.konig.triplestore.gae;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class Schema {

	public static final URI knows = new URIImpl("http://schema.org/knows");
	public static final URI name = new URIImpl("http://schema.org/name");
}
