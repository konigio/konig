package io.konig.core;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class Context {
	
	private Map<String, Term> map = new HashMap<String, Term>();
	private List<Term> list = new ArrayList<Term>();
	private Context inverse;
	private boolean compiled;
	private String iri;
	
	public Context(String iri) {
		this.iri = iri;
	}
	
	public String getLanguage() {
		return null;
	}
	
	public String getContextIRI() {
		return this.iri;
	}
	
	public Term addTerm(String key, String id) {
		Term term = new Term(key, id, null, null);
		add(term);
		return term;
	}
	
	public List<Term> asList() {
		return list;
	}
	
	public String alias(String keyword) {
		Context inverse = inverse();
		Term term = inverse.getTerm(keyword);
		return term == null ? keyword : term.getKey();
	}
	
	public Context inverse() {
		if (!compiled) {
			compile();
		}
		if (inverse == null) {
			inverse = new Context(this.iri);
			for (Term term : list) {
				inverse.list.add(term);
				URI id = term.getExpandedId();
				if (id != null) {
					inverse.map.put(id.stringValue(), term);
				} else {
					String iri = term.getId();
					if (iri != null) {
						inverse.map.put(iri, term);
					}
				}
			}
		}
		
		return inverse;
	}
	
	public void add(Term term) {
		int index = list.size();
		term.setIndex(index);
		map.put(term.getKey(), term);
		list.add(term);
		compiled = false;
		inverse = null;
	}
	
	public Term getTerm(String key) {
		return map.get(key);
	}
	
	public String expandIRI(String value) {
		Term term = getTerm(value);
		if (term != null) {
			String id = term.getId();
			if (id != null) {
				return expandIRI(id);
			}
		}
		int colon = value.indexOf(':');
		if (colon > 0) {
			String prefix = value.substring(0,  colon);
			String expandedPrefix = expandIRI(prefix);
			if (expandedPrefix != prefix) {
				String localName = value.substring(colon+1);
				return expandedPrefix + localName;
			}
		}
		
		return value;
	}
	
	public void compile() {
		if (!compiled) {
			compiled = true;
			
			for (int i=0; i<list.size(); i++) {
				Term term = list.get(i);
				term.setIndex(i);
				String id = term.getId();
				if (id != null) {
					String expandedId = expandIRI(id);
					term.setExpandedId(new URIImpl(expandedId));
				}
				String type = term.getType();
				if (type != null) {
					String expandedType = expandIRI(type);
					if (expandedType != type) {
						term.setExpandedType(new URIImpl(expandedType));
					}
				}
			}
		}
		
	}
	
	public void sort() {
		Collections.sort(list);
		for (int i=0; i<list.size(); i++) {
			list.get(i).setIndex(i);
		}
	}

	

}
