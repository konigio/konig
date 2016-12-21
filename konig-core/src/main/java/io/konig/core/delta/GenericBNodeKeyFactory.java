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
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;

public class GenericBNodeKeyFactory implements BNodeKeyFactory {
	
	private ShaBNodeHasher hasher = new ShaBNodeHasher();

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {
		
		String hash = hasher.createHash(predicate, object);
		
		Map<String, URI> part = new HashMap<>();

		Set<Edge> set = object.outEdgeSet();
		for (Edge e : set) {
			part.put(e.getPredicate().stringValue(), Konig.KeyValue);
		}
		
		
		return 	new BNodeKey(hash, part, this);
	}

}
