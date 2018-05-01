package io.konig.core.impl;

import java.io.IOException;
import java.io.StringWriter;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Term;

public class BasicContext implements Context {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(BasicContext.class);
	
	private Map<String, Term> map = new HashMap<String, Term>();
	private List<Term> list = new ArrayList<Term>();
	private BasicContext inverse;
	private boolean compiled;
	private String iri;
	private String vendorType;
	private long versionNumber;
	
	public BasicContext(String iri) {
		this.iri = iri;
	}
	


	@Override
	public Context deepClone() {
		BasicContext clone = new BasicContext(iri);
		for (Term value : map.values()) {
			clone.add(value.clone());
		}
		clone.versionNumber = versionNumber;
		clone.vendorType = vendorType;
		if (compiled) {
			clone.compile();
		}
		return clone;
	}
	
	public String getLanguage() {
		return null;
	}
	
	public String getContextIRI() {
		return this.iri;
	}
	
	public void setContextIRI(String iri) {
		this.iri = iri;
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
		BasicContext inverse = inverse();
		Term term = inverse.getTerm(keyword);
		return term == null ? keyword : term.getKey();
	}
	
	public BasicContext inverse() {
		if (!compiled) {
			compile();
		}
		if (inverse == null) {
			inverse = new BasicContext(this.iri);
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

	@Override
	public void toJson(JsonGenerator json) throws IOException {
		json.writeStartObject();
		for (Term term : list) {
			json.writeFieldName(term.getKey());
			term.toJson(json);
		}
		
		json.writeEndObject();
		
	}
	
	public String toString() {

		try {
			StringWriter writer = new StringWriter();
			JsonFactory factory = new JsonFactory();
			JsonGenerator json = factory.createGenerator(writer);
			json.useDefaultPrettyPrinter();
			json.writeStartObject();
			json.writeFieldName("@context");
			toJson(json);
			if (iri != null) {
				json.writeStringField("@id", iri);
			}
			if (vendorType != null) {
				json.writeStringField("vendorType", vendorType);
			}
			if (versionNumber > 0) {
				json.writeNumberField("versionNumber", versionNumber);
			}
			json.writeEndObject();
			json.flush();
			return writer.toString();
		} catch (Throwable oops) {
			logger.warn("Failed to produce string representation of BasicContext", oops);
			return BasicContext.class.getSimpleName();
		}
	}

	@Override
	public String getVendorType() {
		return vendorType;
	}

	@Override
	public void setVendorType(String mediaType) {
		this.vendorType = mediaType;
		
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
