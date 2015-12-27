package io.konig.core.impl;

import io.konig.core.IriRewriter;
import io.konig.core.RewriteService;

public class SimpleRewriteService implements RewriteService {
	private IriRewriter to;
	private IriRewriter from;
	
	public SimpleRewriteService(String localBaseIRI, String canonicalBaseIRI) {
		to = new BaseIriRewriter(canonicalBaseIRI, localBaseIRI);
		from = new BaseIriRewriter(localBaseIRI, canonicalBaseIRI);
	}

	@Override
	public String toLocal(String iri) {
		return to.rewrite(iri);
	}

	@Override
	public String fromLocal(String iri) {
		return from.rewrite(iri);
	}

}
