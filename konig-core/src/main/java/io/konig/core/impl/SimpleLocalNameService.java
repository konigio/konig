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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.LocalNameService;

public class SimpleLocalNameService implements LocalNameService {
	
	private static final Set<URI> EMPTYSET = new HashSet<>();
	
	private Map<String, Set<URI>> map = new HashMap<>();

	public SimpleLocalNameService() {
	}
	
	public void add(String localName, URI uri) {
		Set<URI> set = map.get(localName);
		if (set == null) {
			set = new HashSet<>();
			map.put(localName, set);
		}
		set.add(uri);
	}

	@Override
	public Set<URI> lookupLocalName(String localName) {
		Set<URI> result = map.get(localName);
		
		return result==null ? EMPTYSET : result;
	}

}
