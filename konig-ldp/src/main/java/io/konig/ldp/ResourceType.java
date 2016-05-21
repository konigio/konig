package io.konig.ldp;

/*
 * #%L
 * Konig Linked Data Platform
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

import org.openrdf.model.URI;

import io.konig.core.vocab.LDP;

public enum ResourceType  {
	Resource(LDP.Resource),
	RDFSource(LDP.RDFSource, Resource),
	NonRDFSource(LDP.NonRDFSource, Resource),
	Container(LDP.Container, RDFSource),
	BasicContainer(LDP.BasicContainer, Container),
	DirectContainer(LDP.DirectContainer, Container),
	IndirectContainer(LDP.IndirectContainer, DirectContainer);
	
	private URI uri;
	private ResourceType superType;
	
	private ResourceType(URI uri) {
		this.uri = uri;
	}
	private ResourceType(URI uri, ResourceType superType) {
		this.uri = uri;
		this.superType = superType;
	}
	
	public URI getURI() {
		return uri;
	}
	public ResourceType getSuperType() {
		return superType;
	}
	
	public boolean isSubClassOf(ResourceType superClass) {
		return this==superClass || (
			superType != null && 
			(superType==superClass || superType.isSubClassOf(superClass))
		);
	}
	
	public static ResourceType fromURI(String uriValue) {
		ResourceType[] list = values();
		for (int i=0; i<list.length; i++) {
			ResourceType type = list[i];
			if (type.uri.stringValue().equals(uriValue)) {
				return type;
			}
		}
		return null;
	}

}
