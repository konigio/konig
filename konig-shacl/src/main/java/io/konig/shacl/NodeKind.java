package io.konig.shacl;

/*
 * #%L
 * konig-shacl
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
import org.openrdf.model.impl.URIImpl;

public enum NodeKind  {

	IRI("http://www.w3.org/ns/shacl#IRI"),
	BlankNode("http://www.w3.org/ns/shacl#BlankNode"),
	Literal("http://www.w3.org/ns/shacl#Literal");

	private URI uri;
	private NodeKind(String value) {
		uri = new URIImpl(value);
	}
	
	public URI getURI() {
		return uri;
	}
	
	public static NodeKind fromURI(URI uri) {
		if (IRI.uri.equals(uri)) {
			return IRI;
		}
		if (BlankNode.uri.equals(uri)) {
			return BlankNode;
		}
		if (Literal.uri.equals(uri)) {
			return Literal;
		}
		
		return null;
	}
}
