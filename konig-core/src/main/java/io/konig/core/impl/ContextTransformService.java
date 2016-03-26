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


import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.Term;

public class ContextTransformService  {

	private static final int MAX_SUFFIX = 100;
	

	public void append(Context source, Context target) {
		List<Term> list = source.asList();
		source.compile();
		Context sourceInverse = null;
		Context targetInverse = target.inverse();
		
		for (Term term : list) {
			String key = term.getKey();
			String value = term.getExpandedIdValue();
			
			if (targetInverse.getTerm(value) != null) {
				// The target already contains a term for the
				// specified value, so skip this term.
				
				continue;
			}
			
			Term other = target.getTerm(key);
			if (other == null) {
				target.add(new Term(key, value, term.getKind()));
			} else {
				// The same term already exists mapped to a different value
				
				URI uri = new URIImpl(value);
				String namespace = uri.getNamespace();
				
				if (sourceInverse == null) {
					sourceInverse = source.inverse();
				}
				
				String localName = uri.getLocalName();
				
				Term namespaceTerm = sourceInverse.getTerm(namespace);
				
				if (namespaceTerm != null) {
					
					// Try constructing a term of the form <namespacePrefix>_<localName>
					
					StringBuilder builder = new StringBuilder();
					builder.append(namespaceTerm.getKey());
					builder.append('_');
					builder.append(localName);
					
					String newKey = builder.toString();
					
					Term newOther = target.getTerm(newKey);
					if (newOther == null) {
						// There is no other term of the form <namespacePrefix>_<localName>
						// Go ahead and mint such a term now.
						target.add(new Term(newKey, value, term.getKind()));
						continue;
					}
				}
				
				// Try minting a term of the form <localName><suffix>
				// where <suffix> is an integer.
				
				for (int suffix=1; suffix<MAX_SUFFIX; suffix++) {
					StringBuilder builder = new StringBuilder(localName);
					builder.append(suffix);
					
					String newKey = builder.toString();
					if (target.getTerm(newKey)==null) {
						target.add(new Term(newKey, value, term.getKind()));
						break;
					}
				}
			}
			
		}

	}

}
