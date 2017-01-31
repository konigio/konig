package io.konig.datasource;

import java.util.LinkedHashSet;
import java.util.Set;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.DC;
import io.konig.core.vocab.Konig;

public class DataSource {
	
	private Resource id;
	private String identifier;
	private Set<URI> type = new LinkedHashSet<>();
	private IriTemplate iriTemplate;

	public boolean isA(URI type) {
		return this.type.contains(type) || Konig.DataSource.equals(type);
	}

	@RdfProperty(DC.IDENTIFIER)
	public String getIdentifier() {
		return identifier;
	}

	@RdfProperty(DC.IDENTIFIER)
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void addType(URI type) {
		this.type.add(type);
	}

	@RdfProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	public Set<URI> getType() {
		return type;
	}

	public Resource getId() {
		return id;
	}

	public void setId(Resource id) {
		this.id = id;
	}

	@RdfProperty(Konig.IRI_TEMPLATE)
	public IriTemplate getIriTemplate() {
		return iriTemplate;
	}

	@RdfProperty(Konig.IRI_TEMPLATE)
	public void setIriTemplate(IriTemplate iriTemplate) {
		this.iriTemplate = iriTemplate;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof DataSource) {
			return id.equals(((DataSource) other).getId());
		}
		return false;
	}
	
}
