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

import io.konig.core.vocab.SH;

public enum NodeKind  {

	IRI(SH.IRI, 1),
	BlankNode(SH.BlankNode, 2),
	BlankNodeOrIRI(SH.BlankNodeOrIRI, 3),
	Literal(SH.Literal, 4),
	IRIOrLiteral(SH.IRIOrLiteral, 5),
	BlankNodeOrLiteral(SH.BlankNodeOrLiteral, 6),
	;

	private URI uri;
	private int index;
	
	
	private NodeKind(URI uri, int index) {
		this.uri = uri;
		this.index = index;
		
	}
	
	public URI getURI() {
		return uri;
	}
	
	public static NodeKind or(NodeKind a, NodeKind b) {
		int index = ((a==null ? 0 : a.index) | (b==null ? 0 : b.index)) - 1;
		
		return (index<0) ? null : values()[index];
		
		
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
		
		if (BlankNodeOrIRI.uri.equals(uri)) {
			return BlankNodeOrIRI;
		}
		
		return null;
	}
}
