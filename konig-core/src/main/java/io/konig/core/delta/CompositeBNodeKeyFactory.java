package io.konig.core.delta;

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
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class CompositeBNodeKeyFactory implements BNodeKeyFactory {
	
	private Map<URI, BNodeKeyFactory> map = new HashMap<>();

	public CompositeBNodeKeyFactory() {
	}
	
	public void register(URI predicate, BNodeKeyFactory delegate) {
		map.put(predicate, delegate);
	}

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {

		BNodeKeyFactory delegate = map.get(predicate);
		if (delegate != null) {
			return delegate.createKey(predicate, object);
		}
		
		return null;
	}

}
