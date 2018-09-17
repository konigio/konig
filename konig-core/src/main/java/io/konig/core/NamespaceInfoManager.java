package io.konig.core;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class NamespaceInfoManager {
	
	private Map<String, NamespaceInfo> map = new HashMap<>();
	
	public void load(Graph graph) {
		OwlReasoner owl = new OwlReasoner(graph);
		for (Vertex v : graph.vertices()) {
			Resource node = v.getId();
			if (node instanceof URI) {
				URI iri = (URI) node;
				String namespace = iri.getNamespace();
				
				NamespaceInfo set = map.get(namespace);
				if (set == null) {
					set = new NamespaceInfo(namespace);
					map.put(namespace, set);
				}
				set.getTerms().add(iri);
				if (owl.isNamedIndividual(iri)) {
					set.getIndividuals().add(iri);
				}
			}
		}
	}
	
	public Collection<NamespaceInfo> listNamespaces() {
		return map.values();
	}
	
	public NamespaceInfo getNamespaceInfo(String namespace) {
		return map.get(namespace);
	}

}
