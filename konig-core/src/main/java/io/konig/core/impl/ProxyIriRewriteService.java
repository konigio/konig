package io.konig.core.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import io.konig.core.RewriteService;

public class ProxyIriRewriteService implements RewriteService {

	private String canonicalBase;
	private String cacheBase;
	private String localBase;

	public ProxyIriRewriteService(String canonicalBase, String cacheBase, String localBase) {
		this.canonicalBase = canonicalBase;
		this.cacheBase = cacheBase;
		this.localBase = localBase;
	}

	@Override
	public String toLocal(String iri) {
		
		if (iri.startsWith(localBase)) {
			return iri;
		}
		if (iri.startsWith(canonicalBase)) {
			return simpleRewrite(canonicalBase, localBase, iri);
		}
		
		int colon = iri.indexOf(':');
		if (colon < 1) {
			throw new RuntimeException("Invalid IRI: " + iri);
		}
		String protocol = iri.substring(0, colon);
		String path = iri.substring(colon+2);
		StringBuilder builder = new StringBuilder();
		builder.append(cacheBase);
		builder.append(protocol);
		builder.append(path);
		
		return builder.toString();
	}

	@Override
	public String fromLocal(String iri) {
		
		if (iri.startsWith(cacheBase)) {
			int start = cacheBase.length();
			int end = iri.indexOf('/', start);
			String protocol = iri.substring(start, end);
			String path = iri.substring(end+1);
			StringBuilder builder = new StringBuilder();
			builder.append(protocol);
			builder.append("://");
			builder.append(path);
			return builder.toString();
		}
		if (iri.startsWith(localBase)) {
			return simpleRewrite(localBase, canonicalBase, iri);
		}
		
		return iri;
		
	}
	
	protected String simpleRewrite(String fromBaseIRI, String toBaseIRI, String iri) {

		String tail = iri.substring(fromBaseIRI.length());
		StringBuilder builder = new StringBuilder(toBaseIRI.length() + tail.length());
		builder.append(toBaseIRI);
		builder.append(tail);
		iri = builder.toString();
		
		return iri;
	}

}
