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


import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;

public class TermStatusProcessor {

	private Graph graph;
	
	/**
	 * Create a new TermStatusProcessor.
	 * @param konigDescriber  A service which describes the Konig ontology.  The TermStatusProcessor depends on the
	 * KonigDescriber only as a way to ensure that it runs.
	 * @param graph The graph into which edges will be written.
	 */
	public TermStatusProcessor(KonigDescriber konigDescriber, Graph graph) {
		this.graph = graph;
		graph.edge(Konig.TermStatus, RDF.TYPE, OWL.CLASS);
		graph.edge(Konig.TermStatus, RDFS.SUBCLASSOF, Schema.Enumeration);
		
	}
	
	public void declareStatus(URI statusValue) {
		if (statusValue != null) {
			graph.edge(statusValue, RDF.TYPE, Konig.TermStatus);
		}
	}
	
	public void assertStatus(URI term, URI statusValue) {
		if (term != null && statusValue!=null) {
			graph.edge(statusValue, RDF.TYPE, Konig.TermStatus);
			graph.edge(term, Konig.termStatus, statusValue);
		}
	}

}
