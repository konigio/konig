package io.konig.services.impl;

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
