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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Term;

public class CompositeContext implements Context {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(CompositeContext.class);
	private List<ChainedContext> list = new ArrayList<>();
	private ChainedContext last;
	private String iri;
	private boolean compiled;
	private String vendorType;
	private long versionNumber;

	public CompositeContext() {
	}


	@Override
	public CompositeContext deepClone() {
		CompositeContext clone = new CompositeContext();
		for (ChainedContext chain : list) {
			ChainedContext chainClone = chain.deepClone();
			last = chainClone;
			list.add(chainClone);
		}
		clone.vendorType = vendorType;
		clone.versionNumber = versionNumber;
		
		return clone;
	}
	
	public void append(Context context) {
		last = new ChainedContext(last, context);
		list.add(last);
	}
	
	public ChainedContext getLast() {
		return last;
	}

	@Override
	public String getLanguage() {
		return last.getLanguage();
	}

	@Override
	public String getContextIRI() {
		return iri;
	}

	@Override
	public void setContextIRI(String iri) {
		this.iri = iri;
	}

	@Override
	public Term addTerm(String key, String id) {
		return last.addTerm(key, id);
	}

	@Override
	public List<Term> asList() {
		return last.asList();
	}

	@Override
	public String alias(String keyword) {
		return last.alias(keyword);
	}

	@Override
	public Context inverse() {
		return last.inverse();
	}

	@Override
	public void add(Term term) {
		last.add(term);
	}

	@Override
	public Term getTerm(String key) {
		return last.getTerm(key);
	}

	@Override
	public String expandIRI(String value) {
		return last.expandIRI(value);
	}

	@Override
	public void compile() {
		if (!compiled) {
			ChainedContext c = last;
			while (c != null) {
				c.compile();
				
				Context parent = c.getParent();
				c = (parent instanceof ChainedContext) ? (ChainedContext) parent : null;
			}
		}
	}

	@Override
	public void sort() {
		
		
	}

	@Override
	public void toJson(JsonGenerator json) throws IOException {
		json.writeStartArray();
		for (ChainedContext context : list) {
			context.toJson(json);
		}
		json.writeEndArray();
		
	}
	
	public String toString() {
		
		try {
			StringWriter writer = new StringWriter();
			JsonFactory factory = new JsonFactory();
			JsonGenerator json = factory.createGenerator(writer);
			json.useDefaultPrettyPrinter();
			toJson(json);
			json.flush();
			return writer.toString();
		} catch (Throwable oops) {
			logger.warn("Failed to produce string representation of CompositeContext", oops);
			return CompositeContext.class.getSimpleName();
		}
	}

	@Override
	public String getVendorType() {
		return vendorType;
	}

	@Override
	public void setVendorType(String mediaType) {
		vendorType = mediaType;
		
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
