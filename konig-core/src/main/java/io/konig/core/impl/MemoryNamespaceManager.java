package io.konig.core.impl;

import java.util.Collection;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.NamespaceManager;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.DC;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.PROV;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;

public class MemoryNamespaceManager implements NamespaceManager {
	private static final MemoryNamespaceManager INSTANCE = new MemoryNamespaceManager();
	
	protected Map<String, Namespace> byPrefix = new HashMap<String, Namespace>();
	protected Map<String,Namespace> byName = new HashMap<>();
	
	static {
		INSTANCE.add("konig", Konig.NAMESPACE);
		INSTANCE.add("as", AS.NAMESPACE);
		INSTANCE.add("sh", SH.NAMESPACE);
		INSTANCE.add("prov", PROV.NAMESPACE);
		INSTANCE.add("activity", Konig.ACTIVIY_BASE_URL);
		INSTANCE.add("schema", Schema.NAMESPACE);
		INSTANCE.add("xsd", XMLSchema.NAMESPACE);
		INSTANCE.add("dc", DC.NAMESPACE);
		INSTANCE.add("owl", OWL.NAMESPACE);
		INSTANCE.add("rdf", RDF.NAMESPACE);
		INSTANCE.add("rdfs", RDFS.NAMESPACE);
	}
	
	public static MemoryNamespaceManager getDefaultInstance() {
		return INSTANCE;
	}
	
	public MemoryNamespaceManager() {
		
	}
	
	public MemoryNamespaceManager(NamespaceManager copy) {
		for (Namespace n : copy.listNamespaces()) {
			add(n);
		}
	}

	public Namespace findByPrefix(String prefix) {
		return byPrefix.get(prefix);
	}

	public Namespace findByName(String name) {
		return byName.get(name);
	}

	public NamespaceManager add(Namespace ns) {
		byPrefix.put(ns.getPrefix(), ns);
		byName.put(ns.getName(), ns);
		return this;
	}

	public NamespaceManager add(String prefix, String namespace) {
		return add(new NamespaceImpl(prefix, namespace));
	}

	@Override
	public Collection<Namespace> listNamespaces() {
		return byPrefix.values();
	}
	

}
