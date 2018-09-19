package io.konig.schema;

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


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceInfo;
import io.konig.core.NamespaceInfoManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.Schema;

public class EnumerationReasoner {
	
	private Map<String, Set<URI>> individualByName;
	
	public void mapIndividualsByName(Graph graph) {
		individualByName = new HashMap<>();
		OwlReasoner owl = new OwlReasoner(graph);
		
		for (Vertex v : graph.vertices()) {
			Resource id = v.getId();
			if (id instanceof URI) {
				if (owl.instanceOf(id, Schema.Enumeration) || owl.instanceOf(id, OwlVocab.NamedIndividual)) {
					Value value  = v.getValue(Schema.name);
					if (value != null) {
						String name = value.stringValue();
						Set<URI> set = individualByName.get(name);
						if (set == null) {
							set = new HashSet<>();
							individualByName.put(name, set);
						}
						set.add((URI) id);
					}
				}
			}
		}
	}
	
	public Set<URI> getIndividualsByName(String name) {
		if (individualByName == null) {
			throw new KonigException("The method `mapIndividualsByName` must be invoked before calling `getIndividualsByName`");
		}
		Set<URI> result =  individualByName.get(name);
		if (result == null) {
			result = Collections.emptySet();
		}
		return result;
	}
	
	public void annotateEnumerationNamespaces(Graph graph, NamespaceInfoManager nim) {
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		outer: for (NamespaceInfo ns : nim.listNamespaces()) {
			inner: for (URI term : ns.getTerms()) {
				Vertex v = graph.getVertex(term);
				if (v != null) {
					Resource id = v.getId();
					
					if (id instanceof URI) {

						if (reasoner.isSubClassOf(id, Schema.Enumeration)) {
							continue inner;
						}
						
						if (id.stringValue().equals(ns.getNamespaceIri())) {
							continue inner;
						}
	
						Set<Edge> typeSet = v.outProperty(RDF.TYPE);
	
						for (Edge e : typeSet) {
							Value value = e.getObject();
							if (value instanceof URI) {
								URI typeId = (URI) value;
								if (OwlVocab.NamedIndividual.equals(typeId)
										|| reasoner.isSubClassOf(typeId, Schema.Enumeration)) {
									continue inner;
								}
							}
						}
	
						// The current term is not a named individual or a subClass of schema:Enumeration
						continue outer;
					}
				}
			}

			// Every term in the namespace is either a subClass of
			// schema:Enumeration or a named individual.
			ns.getType().add(Konig.EnumNamespace);

		}
	}

}
