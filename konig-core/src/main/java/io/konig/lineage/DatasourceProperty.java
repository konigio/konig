package io.konig.lineage;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.RdfVocab;
import io.konig.datasource.DataSource;

public class DatasourceProperty {
	
	private URI id;
	private DatasourcePropertyPath propertyPath;
	private DataSource propertySource;
	private PropertyGenerator generatedFrom;
	private Set<PropertyGenerator> generatorInputOf;

	public URI getId() {
		return id;
	}

	public void setId(URI id) {
		this.id = id;
	}

	@RdfProperty(Konig.Terms.propertyPath)
	public DatasourcePropertyPath getPropertyPath() {
		return propertyPath;
	}
	
	@RdfProperty(RdfVocab.Terms.type)
	public URI getType() {
		return Konig.DatasourceProperty;
	}

	public void setPropertyPath(DatasourcePropertyPath propertyPath) {
		this.propertyPath = propertyPath;
	}


	@RdfProperty(Konig.Terms.propertySource)
	public DataSource getPropertySource() {
		return propertySource;
	}

	public void setPropertySource(DataSource propertySource) {
		this.propertySource = propertySource;
	}

	@RdfProperty(Konig.Terms.generatedFrom)
	public PropertyGenerator getGeneratedFrom() {
		return generatedFrom;
	}

	public void setGeneratedFrom(PropertyGenerator generatedFrom) {
		this.generatedFrom = generatedFrom;
	}
	
	public void addGeneratorInputOf(PropertyGenerator g) {
		if (generatorInputOf == null) {
			generatorInputOf = new LinkedHashSet<>();
		}
		generatorInputOf.add(g);
	}

	@RdfProperty(Konig.Terms.generatorInputOf)
	public Set<PropertyGenerator> getGeneratorInputOf() {
		return generatorInputOf==null ? Collections.emptySet() : generatorInputOf;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatasourceProperty(");
		builder.append(id);
		builder.append(")");
		return builder.toString();
	}

	
}
