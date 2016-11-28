package io.konig.core.impl;

/*
 * #%L
 * Konig Core
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;

import io.konig.core.NamespaceManager;

public class CompositeNamespaceManager extends MemoryNamespaceManager {
	
	private List<NamespaceManager> children = new ArrayList<>();

	public CompositeNamespaceManager(NamespaceManager... nsManager) {
		for (NamespaceManager ns : nsManager) {
			children.add(ns);
		}
	}
	
	public void add(NamespaceManager manager) {
		children.add(manager);
	}

	@Override
	public Namespace findByPrefix(String prefix) {
		Namespace ns = super.findByPrefix(prefix);
		if (ns == null) {
			for (NamespaceManager manager : children) {
				ns = manager.findByPrefix(prefix);
				if (ns != null) {
					return ns;
				}
			}
		}
		return ns;
	}

	@Override
	public Namespace findByName(String name) {
		Namespace ns = super.findByName(name);
		if (ns == null) {
			for (NamespaceManager manager :children) {
				ns = manager.findByName(name);
				if (ns != null) {
					return ns;
				}
			}
		}
		return ns;
	}

	@Override
	public NamespaceManager add(Namespace ns) {
		super.add(ns);
		return this;
	}

	@Override
	public NamespaceManager add(String prefix, String namespace) {
		super.add(prefix, namespace);
		return this;
	}

	@Override
	public Collection<Namespace> listNamespaces() {
		Set<Namespace> set = new HashSet<>();
		set.addAll(super.listNamespaces());
		for (NamespaceManager manager : children) {
			Collection<Namespace> collection = manager.listNamespaces();
			set.addAll(collection);
		}
		return set;
	}

}
