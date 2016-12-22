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
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Namespace;

import io.konig.core.NamespaceManager;

public class NamespaceManagerList extends ArrayList<NamespaceManager> implements NamespaceManager {
	private static final long serialVersionUID = 1L;

	public NamespaceManagerList() {
	}

	public NamespaceManagerList(int arg0) {
		super(arg0);
	}

	public NamespaceManagerList(Collection<NamespaceManager> arg0) {
		super(arg0);
	}

	@Override
	public Namespace findByPrefix(String prefix) {
		for (NamespaceManager m : this) {
			Namespace ns = m.findByPrefix(prefix);
			if (ns != null) {
				return ns;
			}
		}
		return null;
	}

	@Override
	public Namespace findByName(String name) {
		for (NamespaceManager m : this) {
			Namespace ns = m.findByName(name);
			if (ns != null) {
				return ns;
			}
		}
		return null;
	}

	@Override
	public NamespaceManager add(Namespace ns) {
		get(0).add(ns);
		return this;
	}

	@Override
	public NamespaceManager add(String prefix, String namespace) {
		get(0).add(prefix, namespace);
		return this;
	}

	@Override
	public Collection<Namespace> listNamespaces() {
		Map<String,Namespace> map = new HashMap<>();
		for (NamespaceManager m : this) {
			Collection<Namespace> list = m.listNamespaces();
			for (Namespace ns : list) {
				if (!map.containsKey(ns.getName())) {
					map.put(ns.getName(), ns);
				}
			}
		}
		return map.values();
	}

}
