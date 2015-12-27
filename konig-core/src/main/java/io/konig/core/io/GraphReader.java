package io.konig.core.io;

import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.Vertex;


public class GraphReader extends BaseGraphReader {
	
	private Graph graph;	
	

	/**
	 * Read binary data into a specified Graph.
	 * @param data The data that is to be read
	 * @param graph The graph into which the data will be read
	 * @param manager A ContextManager that can be used to fetch the Context that governs the data
	 * @throws IOException
	 */
	public void read(byte[] data, Graph graph, ContextManager manager) throws KonigReadException {
		this.graph = graph;
		read(data, manager);
	}


	protected Object beginNamedGraph(Resource subject) {
		Vertex v = graph.vertex(subject);
		Graph oldGraph = graph;
		graph = v.assertNamedGraph();
		return oldGraph;
	}
	
	protected void endNamedGraph(Object state) {
		graph = (Graph) state;
	}


	@Override
	protected void handleStatement(Resource subject, URI predicate, Value object) {
		graph.edge(subject, predicate, object);
	}
	
	

}
