package io.konig.shacl;

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


import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;

public class SimpleShapeNamer implements ShapeNamer {
	private String baseURL;
	private NamespaceManager nsManager;
	private String prefix="";
	private String suffix="";
	

	public SimpleShapeNamer(NamespaceManager nsManager, String baseURL) {
		this.nsManager = nsManager;
		this.baseURL = baseURL;
	}
	
	public SimpleShapeNamer(NamespaceManager nsManager, String baseURL, String suffix) {
		this.nsManager = nsManager;
		this.baseURL = baseURL;
		this.suffix = suffix;
	}
	
	public SimpleShapeNamer(NamespaceManager nsManager, String baseURL, String prefix, String suffix) {
		this.nsManager = nsManager;
		this.baseURL = baseURL;
		this.prefix = prefix;
		this.suffix = suffix;
	}



	@Override
	public URI shapeName(URI scopeClass) throws KonigException {
		String namespace = scopeClass.getNamespace();
		Namespace ns = nsManager.findByName(namespace);
		if (ns == null) {
			throw new KonigException("Prefix not found for namespace " + namespace);
		}
		StringBuilder builder = new StringBuilder(baseURL);
		builder.append(ns.getPrefix());
		builder.append('/');
		
		builder.append(prefix);
		builder.append(scopeClass.getLocalName());
		builder.append(suffix);
		
		return new URIImpl(builder.toString());
	}
	
	

}
