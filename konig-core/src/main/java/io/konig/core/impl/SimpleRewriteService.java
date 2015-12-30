package io.konig.core.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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
