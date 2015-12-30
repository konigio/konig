package io.konig.core.impl;

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

import io.konig.core.NamespaceManager;

public class MemoryNamespaceManager implements NamespaceManager {
	protected Map<String, Namespace> map = new HashMap<String, Namespace>();

	public Namespace findByPrefix(String prefix) {
		return map.get(prefix);
	}

	public Namespace findByName(String name) {
		return map.get(name);
	}

	public NamespaceManager add(Namespace ns) {
		map.put(ns.getPrefix(), ns);
		map.put(ns.getName(), ns);
		return this;
	}

	public NamespaceManager add(String prefix, String namespace) {
		return add(new NamespaceImpl(prefix, namespace));
	}
	

}
