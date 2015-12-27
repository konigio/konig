package io.konig.core.impl;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.IriRewriter;

public class CompositeIriRewriter implements IriRewriter {
	
	private List<IriRewriter> list = new ArrayList<>();
	
	public void add(IriRewriter rewriter) {
		list.add(rewriter);
	}

	@Override
	public String rewrite(String iri) {
		for (IriRewriter rewriter : list) {
			String  other = rewriter.rewrite(iri);
			if (other != iri) {
				return other;
			}
		}
		return iri;
	}

}
