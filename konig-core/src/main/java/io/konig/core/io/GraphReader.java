package io.konig.core.io;

/*
 * #%L
 * konig-core
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
