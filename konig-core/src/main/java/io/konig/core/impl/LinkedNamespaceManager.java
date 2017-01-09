package io.konig.core.impl;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Namespace;

import io.konig.core.NamespaceManager;

public class LinkedNamespaceManager implements NamespaceManager {
	
	private NamespaceManager first;
	private NamespaceManager rest;

	public LinkedNamespaceManager(NamespaceManager first, NamespaceManager rest) {
		this.first = first;
		this.rest = rest;
	}


	@Override
	public Namespace findByPrefix(String prefix) {
		Namespace ns = first.findByPrefix(prefix);
		if (ns == null) {
			ns = rest.findByPrefix(prefix);
		}
		return ns;
	}

	@Override
	public Namespace findByName(String name) {
		Namespace ns = first.findByName(name);
		if (ns == null) {
			ns = rest.findByName(name);
		}
		return ns;
	}

	@Override
	public NamespaceManager add(Namespace ns) {
		first.add(ns);
		return this;
	}

	@Override
	public NamespaceManager add(String prefix, String namespace) {
		first.add(prefix, namespace);
		return this;
	}

	@Override
	public Collection<Namespace> listNamespaces() {
		List<Namespace> list = new ArrayList<>(first.listNamespaces());
		
		for (Namespace ns : rest.listNamespaces()) {
			String name = ns.getName();
			if (first.findByName(name)==null) {
				list.add(ns);
			}
		}
		
		return list;
	}

}
