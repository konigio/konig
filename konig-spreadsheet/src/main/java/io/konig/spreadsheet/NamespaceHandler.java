package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.VANN;

public class NamespaceHandler  {
	
	private Graph graph;
	private Set<String> namespaces = new HashSet<>();

	
	
	public NamespaceHandler(Graph graph) {
		this.graph = graph;
	}

	public void addNamespace(String prefix, String namespace) {
		namespaces.add(namespace);
		graph.getNamespaceManager().add(prefix, namespace);
	}

	public void execute() throws SpreadsheetException {
		
		MemoryGraph sink = new MemoryGraph(graph.getNamespaceManager());
		for (Edge e : graph) {
			declareDefaultOntology(e.getSubject(), sink);
			declareDefaultOntology(e.getPredicate(), sink);
			declareDefaultOntology(e.getObject(), sink);
		}
		
		graph.addAll(sink);

	}
	private void declareDefaultOntology(Value value, MemoryGraph sink) throws SpreadsheetException {
		if (value instanceof URI) {
			URI uri = (URI) value;
			String name = uri.getNamespace();
			if (namespaces.contains(name)) {
				namespaces.remove(name);
				Namespace ns = graph.getNamespaceManager().findByName(name);
				if (ns != null) {
					URI ontologyId = new URIImpl(name);
					sink.edge(ontologyId, RDF.TYPE, OWL.ONTOLOGY);
					sink.edge(ontologyId, VANN.preferredNamespacePrefix, new LiteralImpl(ns.getPrefix()));
				}
				
			}
		}

	}


}
