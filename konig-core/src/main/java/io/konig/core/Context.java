package io.konig.core;

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
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.io.PrettyPrintable;

public interface Context extends Serializable {
	
	/**
	 * Get the version number for this context.
	 * Later versions have a larger version numbers that earlier versions.  But version numbers are not necessarily sequential. 
	 * @return The version number for this Context, or 0 if this Context is not under version control.
	 */
	public long getVersionNumber();
	
	public void setVersionNumber(long versionNumber);
	
	/**
	 * Returns a vendor specific media type associated with this JSON-LD context, or null
	 * if no such media type is known.
	 */
	public String getVendorType();
	
	public void setVendorType(String mediaType);
	
	public String getLanguage();
	
	public String getContextIRI();
	
	public void setContextIRI(String iri);
	
	public Term addTerm(String key, String id);
	
	public List<Term> asList();
	
	public String alias(String keyword);
	
	public Context inverse();
	
	public void add(Term term);
	
	public Term getTerm(String key);
	
	public String expandIRI(String value);
	
	public void compile();
	
	public void sort();	
	
	public void toJson(JsonGenerator json) throws IOException;

}
