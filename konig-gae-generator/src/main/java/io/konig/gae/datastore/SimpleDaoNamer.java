package io.konig.gae.datastore;

/*
 * #%L
 * Konig GAE Generator
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

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;

public class SimpleDaoNamer implements DaoNamer {

	
	private String basePackage;
	private NamespaceManager nsManager;	
	
	
	public SimpleDaoNamer(String basePackage, NamespaceManager nsManager) {
		this.basePackage = basePackage;
		this.nsManager = nsManager;
	}




	@Override
	public String daoName(URI owlClass) {
		Namespace ns = nsManager.findByName(owlClass.getNamespace());
		if (ns == null) {
			throw new KonigException("Namespace prefix not found: " + owlClass.getNamespace());
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(basePackage);
		builder.append('.');
		builder.append(ns.getPrefix());
		builder.append('.');
		builder.append(owlClass.getLocalName());
		builder.append("Dao");
		
		return builder.toString();
	}

}
