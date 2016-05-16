package io.konig.ldp.impl;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.io.JsonldParser;
import io.konig.core.vocab.LDP;
import io.konig.ldp.LdpException;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceType;

public class RdfSourceImpl extends ResourceFileImpl implements RdfSource {

	private Graph graph;
	
	public RdfSourceImpl(String contentLocation, String contentType, ResourceType type, byte[] body) {
		super(contentLocation, contentType, type, body);
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	@Override
	public boolean isRdfSource() {
		return true;
	}

	@Override
	public RdfSource asRdfSource() {
		return this;
	}

	@Override
	public Graph createGraph() throws LdpException, IOException {
		if (graph == null) {
			byte[] data = getEntityBody();
			graph = new MemoryGraph();
			graph.setNamespaceManager(new MemoryNamespaceManager());
			graph.getNamespaceManager().add("ldp", LDP.NAMESPACE);
			
			if (data != null) {

				String contentType = getContentType();
				if ("text/turtle".equals(contentType)) {
					GraphLoadHandler handler = new GraphLoadHandler(graph);
					
					TurtleParser parser = new TurtleParser();
					ByteArrayInputStream input = new ByteArrayInputStream(data);
					
					parser.setRDFHandler(handler);
					try {
						parser.parse(input, "");
					} catch (RDFParseException | RDFHandlerException e) {
						throw new LdpException(e);
					}
					
				} else if ("application/ld+json".equals(contentType)) {
					JsonldParser parser = new JsonldParser(null);
					ByteArrayInputStream input = new ByteArrayInputStream(data);

					GraphLoadHandler handler = new GraphLoadHandler(graph);
					parser.setRDFHandler(handler);
					try {
						parser.parse(input, "");
					} catch (RDFParseException | RDFHandlerException e) {
						throw new LdpException(e);
					}
					
				} else {
					throw new LdpException("Content-Type not supported: " + contentType);
				}
			}
			
		}
		
		
		return graph;
	}

}
