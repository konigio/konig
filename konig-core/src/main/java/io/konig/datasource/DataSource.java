package io.konig.datasource;

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

import io.konig.annotation.OwlClass;
import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.DC;
import io.konig.core.vocab.Konig;

@OwlClass(Konig.DATA_SOURCE)
public abstract class DataSource {
	
	private Resource id;
	private String identifier;


	@RdfProperty(DC.IDENTIFIER)
	public String getIdentifier() {
		return identifier;
	}

	@RdfProperty(DC.IDENTIFIER)
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@RdfProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	abstract public URI getType();

	public Resource getId() {
		return id;
	}

	public void setId(Resource id) {
		this.id = id;
	}

}
