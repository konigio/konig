package io.konig.showl;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.extract.ExtractException;
import io.konig.core.extract.OntologyExtractor;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.FileGetter;
import io.konig.core.vocab.SH;

public class OntologyWriter {

	private FileGetter fileGetter;
	private OntologyExtractor extractor;
	
	public OntologyWriter(FileGetter fileGetter) {
		this.fileGetter = fileGetter;
		extractor = new OntologyExtractor();
	}

	public void writeOntologies(Graph graph) throws IOException, KonigException {
		
		List<Vertex> list = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
		for (Vertex v : list) {
			writeOntology(v);
		}
		
	}


	private void writeOntology(Vertex v) throws IOException {
		
		Resource id = v.getId();
		if (!(id instanceof URI)) {
			throw new KonigException("Ontology must be identified by a URI");
		}
		URI ontologyId = (URI) id;
		
		
		Graph graph = new MemoryGraph();
		graph.setNamespaceManager(v.getGraph().getNamespaceManager());
		
		try {
			extractor.extract(v, graph);
			File file = fileGetter.getFile(ontologyId);
			
			FileWriter writer = new FileWriter(file);
			try {
				RdfUtil.prettyPrintTurtle(graph, writer);
				
			} finally {
				writer.close();
			}
			
			
			
		} catch (ExtractException | RDFHandlerException e) {
			throw new KonigException(e);
		}
		
	}

}
