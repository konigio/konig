package io.konig.datasource;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.openrdf.model.impl.URIImpl;

import io.konig.annotation.RdfProperty;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.DC;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeBuilder;

public class DataSource {
	
	private Resource id;
	private String identifier;
	private Set<URI> type = new LinkedHashSet<>();
	private IriTemplate iriTemplate;
	private List<URI> isPartOf;
 	
	public DataSource() {
		
	}
	
	public static class Builder {
		
		private ShapeBuilder shapeBuilder;
		private DataSource ds;
		
		public Builder(ShapeBuilder shapeBuilder) {
			this.shapeBuilder = shapeBuilder;
			ds = new DataSource();
		}
		
		public Builder id(Resource id) {
			ds.setId(id);
			return this;
		}
		
		public Builder id(String idValue) {
			ds.setId(new URIImpl(idValue));
			return this;
		}
		
		public Builder type(URI type) {
			ds.addType(type);
			return this;
		}
		
		public ShapeBuilder endDataSource() {
			shapeBuilder.peekShape().addShapeDataSource(ds);
			return shapeBuilder;
		}
		
	}

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
	
	@RdfProperty(Schema.IS_PART_OF)
	public List<URI> getIsPartOf() {
		return isPartOf==null ? Collections.emptyList() : isPartOf;
	}

	public void setIsPartof(List<URI> isPartOf) {
		this.isPartOf = isPartOf;
	}
	
	@Override
	public int hashCode() {
		return id==null ? super.hashCode() : id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof DataSource) {
			return id.equals(((DataSource) other).getId());
		}
		return false;
	}
	
}
