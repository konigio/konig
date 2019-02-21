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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;

public class ImportAction implements Action {
	private WorkbookProcessor processor;
	private WorkbookLocation location;
	private Graph graph;
	private URI ontologyId;
	private List<String> importList = new ArrayList<>();

	
	

	public ImportAction(WorkbookProcessor processor, WorkbookLocation location, Graph graph, URI ontologyId,
			List<String> importList) {
		this.processor = processor;
		this.location = location;
		this.graph = graph;
		this.ontologyId = ontologyId;
		this.importList = importList;
	}


	@Override
	public void execute() throws SpreadsheetException {
		NamespaceManager nsManager = graph.getNamespaceManager();
		for (String prefix : importList) {
			if (prefix.equals("*")) {
				
				if (importList.size() > 1) {
					processor.fail(location, "Cannot combine wildcard with namespace prefix");
				}
				List<Vertex> objectList = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
				for (Vertex v : objectList) {
					if (!ontologyId.equals(v.getId()) && v.getId() instanceof URI) {
						graph.edge(ontologyId, OWL.IMPORTS, v.getId());
					}
				}
			} else {
				Namespace ns = nsManager.findByPrefix(prefix);
				if (ns == null) {
					processor.fail(location, "Namespace prefix not found: {0}", prefix);
				}
				
				URI object = processor.uri(ns.getName());
				graph.edge(ontologyId, OWL.IMPORTS, object);
			}
			
		}

	}

}
