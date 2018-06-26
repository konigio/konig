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

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.extract.ExtractException;
import io.konig.core.extract.OntologyExtractor;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.FileGetter;
import io.konig.core.vocab.SH;

public class AbbreviationsWriter {
	
	private OntologyExtractor extractor;
	private File baseDir;
	private File abbrevFile;
	private Graph owlGraph;
	
	public AbbreviationsWriter(File baseDir) {
		extractor = new OntologyExtractor();
		this.baseDir=baseDir;
		this.abbrevFile=null;
		owlGraph=new MemoryGraph();
	}

	public void writeAbbreviations(Graph graph) throws IOException, KonigException, RDFHandlerException {

		owlGraph.setNamespaceManager(graph.getNamespaceManager());
		List<Vertex> list = graph.v(SKOS.CONCEPT_SCHEME).in(RDF.TYPE).toVertexList();
		if(list!=null && !list.isEmpty()){
			for (Vertex v : list) {
				writeAbbreviation(v);
			}
			List<Vertex> listConcept = graph.v(SKOS.CONCEPT).in(RDF.TYPE).toVertexList();
			if(listConcept!=null && !listConcept.isEmpty()){
				for (Vertex v : listConcept) {
					writeAbbreviation(v);
				}
			}
			FileWriter writer = new FileWriter(abbrevFile);
			try {
				RdfUtil.prettyPrintTurtle(owlGraph, writer);
				
			} finally {
				writer.close();
			}
		}
		
	}

	private void writeAbbreviation(Vertex v) throws IOException {
		
		Resource id = v.getId();
		if (!(id instanceof URI)) {
			throw new KonigException("Abbreviation must be identified by a URI");
		}
		URI abbrevId = (URI) id;
		
		try {
			extractor.extract(v, owlGraph);
			if(abbrevFile==null){
				NamespaceManager nsManager=owlGraph.getNamespaceManager();
				Namespace namespace = nsManager.findByName(abbrevId.stringValue());
				if (namespace == null) {
					throw new KonigException("Prefix not found for namespace: " + abbrevId.stringValue());
				}
				String prefix = namespace.getPrefix();
				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				builder.append(".ttl");
				abbrevFile=new File(baseDir, builder.toString());
			
			}
			
		} catch (ExtractException e) {
			throw new KonigException(e);
		}
		
	}

}
