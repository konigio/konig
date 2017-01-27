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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Term;

public class ChainedContext implements Context {
	private static final long serialVersionUID = 1L;
	private Context parent;
	private Context self;
	
	private List<Term> termList;
	private Context inverse;
	
	private long versionNumber;
	private boolean compiled = false;
	
	
	public ChainedContext(Context parent, Context self) {
		this.parent = parent;
		this.self = self;
	}

	public Context getParent() {
		return parent;
	}
	
	public Context getDefaultContext() {
		return self;
	}

	@Override
	public String getLanguage() {
		String language = self.getLanguage();
		if (language == null && parent!=null) {
			language = parent.getLanguage();
		}
		return language;
	}

	@Override
	public String getContextIRI() {
		return self.getContextIRI();
	}

	@Override
	public void setContextIRI(String iri) {
		self.setContextIRI(iri);
	}

	@Override
	public Term addTerm(String key, String id) {
		Term term = new Term(key, id, null);
		add(term);
		return term;
	}

	@Override
	public List<Term> asList() {
		
		if (termList == null) {
			synchronized(this) {
				if (termList == null) {
					termList = new ArrayList<>(self.asList());
					if (parent != null) {

						List<Term> parentList = parent.asList();
						for (Term term : parentList) {
							Term other = self.getTerm(term.getKey());
							if (other == null) {
								termList.add(term);
							}
						}
					}
				}
			}
			
		}
		
		return termList;
	}

	@Override
	public String alias(String keyword) {
		String alias = self.alias(keyword);
		if (alias == null && parent!=null) {
			alias = parent.alias(keyword);
		}
		return alias;
	}

	@Override
	public Context inverse() {
		if (inverse == null) {
			synchronized (this) {
				if (inverse == null) {
					Context union = new BasicContext(null);
					List<Term> list = asList();
					for (Term term : list) {
						union.add(term);
					}
					inverse = union.inverse();
				}
			}
		}
		return inverse;
	}

	@Override
	public void add(Term term) {
		self.add(term);
		compiled = false;
		inverse = null;
	}

	@Override
	public Term getTerm(String key) {
		Term term = self.getTerm(key);
		if (term == null && parent!=null) {
			term = parent.getTerm(key);
		}
		return term;
	}

	@Override
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

	@Override
	public void compile() {
		if (parent != null) {
			parent.compile();
		}
		
		if (!compiled) {
			compiled = true;
			
			List<Term> list = self.asList();
			
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

	@Override
	public void sort() {
		self.sort();
	}

	@Override
	public void toJson(JsonGenerator json) throws IOException {
		
		String iri = self.getContextIRI();
		if (iri != null) {
			json.writeString(iri);
		} else {
			self.toJson(json);
		}
		
		
	}

	@Override
	public String getVendorType() {
		
		return parent!=null ? parent.getVendorType() : self.getVendorType();
	}

	@Override
	public void setVendorType(String mediaType) {
		throw new RuntimeException("Method not supported");
		
	}


	@Override
	public long getVersionNumber() {
		return versionNumber;
	}

	@Override
	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}
}
