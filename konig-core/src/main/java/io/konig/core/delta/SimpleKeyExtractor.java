package io.konig.core.delta;

/*
 * #%L
 * konig-core
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
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Vertex;

public class SimpleKeyExtractor implements KeyExtractor {
	
	private URI pathPredicate;
	private URI keyPredicate;
	private List<URI> list;
	
	public SimpleKeyExtractor(URI pathPredicate, URI keyPredicate) {
		this.pathPredicate = pathPredicate;
		this.keyPredicate = keyPredicate;
		list = new ArrayList<>();
		list.add(keyPredicate);
	}

	@Override
	public BNodeKey extractKeys(URI predicate, Vertex object) {
		
		if (pathPredicate.equals(predicate)) {
			Set<Edge> set = object.outProperty(keyPredicate);
			if (set != null) {
				
				StringBuilder builder = new StringBuilder();
				for (Edge e : set) {
					builder.append(e.getObject().stringValue());
					builder.append('|');
				}
				String text = builder.toString();
				return new BNodeKey(text, list);
			}
		}
		return null;
	}

}
