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

public class ClassRequest extends PageRequest {

	private Vertex owlClass;
	private ResourceWriterFactory writerFactory;
	private PropertyManager propertyManager;

	public ClassRequest(PageRequest request, Vertex owlClass, ResourceWriterFactory writerFactory) throws DataCatalogException {
		super(request);
		this.owlClass = owlClass;
		this.writerFactory = writerFactory;
		
		propertyManager = new PropertyManager();
		propertyManager.build(request.getClassStructure());
		
	}

	public Vertex getOwlClass() {
		return owlClass;
	}

	public ResourceWriterFactory getWriterFactory() {
		return writerFactory;
	}

	public String localName(URI resourceId) {
		return resourceId.getLocalName();
	}

	public PropertyManager getPropertyManager() {
		return propertyManager;
	}
	
	
	
}
