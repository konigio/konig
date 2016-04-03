package io.konig.services.impl;

/*
 * #%L
 * Konig Services
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import java.text.MessageFormat;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.Vertex;
import io.konig.core.vocab.AS;
import io.konig.services.GraphService;
import io.konig.services.OntologyService;
import io.konig.services.StorageException;

public class OntologyServiceImpl implements OntologyService {
	private GraphService graphService;
	
	private static final String historyFormat = "{0}v/{1}/history";
	
	public OntologyServiceImpl(GraphService graphService) {
		this.graphService = graphService;
	}

	public void createOntology(Vertex activity) throws StorageException {
		
		Vertex ontology = activity.asTraversal().firstVertex(AS.object);
		URI ontologyID = (URI) ontology.getId();
		Value version = ontology.asTraversal().firstValue(OWL.VERSIONINFO);
		
		String history = MessageFormat.format(historyFormat, ontologyID.stringValue(), version.stringValue());
		URI historyIRI = new URIImpl(history);
		
		
		graphService.put(ontology);
		graphService.post(historyIRI, activity);
		
	}

}
