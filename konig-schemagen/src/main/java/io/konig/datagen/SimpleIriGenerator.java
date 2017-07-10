package io.konig.datagen;

/*
 * #%L
 * Konig Schema Generator
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
import org.openrdf.model.impl.URIImpl;

import io.konig.shacl.Shape;

public class SimpleIriGenerator implements IriGenerator {

	private String baseURL;

	public SimpleIriGenerator(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	public URI createIRI(URI owlClass, int index) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(baseURL);
		builder.append(owlClass.getLocalName());
		builder.append('/');
		builder.append(index);
		return new URIImpl(builder.toString());
	}
	
	
}
