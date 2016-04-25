package io.konig.shacl.impl;

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


import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.shacl.LogicalShapeNamer;

public class BasicLogicalShapeNamer implements LogicalShapeNamer {
	
	private String defaultPrefix = "default";
	private String baseURI;
	private NamespaceManager nsManager;
	
	

	public BasicLogicalShapeNamer(String baseURI, NamespaceManager nsManager) {
		
		if (!baseURI.endsWith("/")) {
			baseURI = baseURI + "/";
		}
		this.baseURI = baseURI;
		this.nsManager = nsManager;
	}
	
	@Override
	public URI logicalShapeForOwlClass(URI owlClass) {
		if (owlClass == null) {
			return null;
		}
		
		Namespace ns = nsManager.findByName(owlClass.getNamespace());
		String prefix = ns==null ? defaultPrefix : ns.getPrefix();
		
		StringBuilder builder = new StringBuilder();
		builder.append(baseURI);
		builder.append(prefix);
		builder.append('/');
		builder.append(owlClass.getLocalName());
		
		return new URIImpl(builder.toString());
	}

}
