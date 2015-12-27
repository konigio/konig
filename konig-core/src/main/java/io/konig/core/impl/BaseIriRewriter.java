package io.konig.core.impl;

import io.konig.core.IriRewriter;

public class BaseIriRewriter implements IriRewriter {
	
	private String fromBaseIRI;
	private String toBaseIRI;

	public BaseIriRewriter(String fromBaseIRI, String toBaseIRI) {
		this.fromBaseIRI = fromBaseIRI;
		this.toBaseIRI = toBaseIRI;
	}


	@Override
	public String rewrite(String iri) {
		if (iri.startsWith(fromBaseIRI)) {
			String tail = iri.substring(fromBaseIRI.length());
			StringBuilder builder = new StringBuilder(toBaseIRI.length() + tail.length());
			builder.append(toBaseIRI);
			builder.append(tail);
			iri = builder.toString();
		}
		return iri;
	}

}
