package io.konig.core.binary;

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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

public class Term {

	private int termType;
	private String localName;
	private Term datatype;
	private Language language;
	private int index;
	private URI datatypeURI;
	
	private Map<String,Term> kids;

	public Term(String localName) {
		this.localName = localName;
	}
	
	public void addType(int bits) {
		termType = termType | bits;
	}
	
	public boolean hasType(int bits) {
		return (bits & termType) != 0;
	}
	
	public void removeType(int bits) {
		termType = termType & (~bits);
	}
	
	public void add(Term child) {
		if (kids==null) {
			kids = new HashMap<>();
		}
		kids.put(child.getLocalName(), child);
	}
	
	public Term get(String localName) {
		return kids==null ? null : kids.get(localName);
	}

	public int getTermType() {
		return termType;
	}

	public void setTermType(int termType) {
		this.termType = termType;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public Term getDatatype() {
		return datatype;
	}

	public void setDatatype(Term datatype) {
		this.datatype = datatype;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}
	
	public int size() {
		return kids == null ? 0 : kids.size();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public Collection<Term> list() {
		return (kids == null) ? null : kids.values();
	}

	public URI getDatatypeURI() {
		return datatypeURI;
	}

	public void setDatatypeURI(URI datatypeURI) {
		this.datatypeURI = datatypeURI;
	}
	
	
}
