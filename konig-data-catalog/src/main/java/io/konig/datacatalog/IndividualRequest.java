package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class IndividualRequest extends PageRequest {

	private Vertex individual;
	private URI enumerationClass;

	public IndividualRequest(PageRequest other, Vertex individual, URI enumerationClass) {
		super(other);
		this.individual = individual;
		this.enumerationClass = enumerationClass;
	}

	public Vertex getIndividual() {
		return individual;
	}


	public URI getEnumerationClass() {
		return enumerationClass;
	}
	
	
}
