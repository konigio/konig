package io.konig.shacl.context;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * A ContextNamer that appends a suffix to the URI for a data shape to form
 * the URI for the corresponding JSON-LD context.
 * @author Greg McFall
 *
 */
public class SuffixContextNamer implements ContextNamer {
	
	private String suffix;

	public SuffixContextNamer(String suffix) {
		this.suffix = suffix;
	}

	public URI forShape(URI shapeId) {
		String value = shapeId.stringValue() + suffix;
		return new URIImpl(value);
	}


}
