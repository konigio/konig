package io.konig.core.io;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

import io.konig.core.Graph;

public class GraphLoadHandler extends RDFHandlerBase {
	
	private Graph graph;
	
	public GraphLoadHandler(Graph graph) {
		super();
		this.graph = graph;
	}

	@Override
	public void handleStatement(Statement statement) throws RDFHandlerException {
		Resource subject = statement.getSubject();
		URI predicate = statement.getPredicate();
		Value object = statement.getObject();
		graph.edge(subject, predicate, object);
	}

}
