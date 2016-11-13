package io.konig.core.io;

/*
 * #%L
 * Konig Core
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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;

public class JsonldLoader {
	
	private URI quadContext;

	/**
	 * Load the contents of a JSON-LD document into a Graph.
	 * @param reader A reader for the JSON-LD document
	 * @param graph The graph into which RDF Statements will be added.
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 */
	public void load(Reader reader, Graph graph) throws RDFParseException, RDFHandlerException, IOException {
		load(reader, graph, null, null);
	}
	
	public void load(Reader reader, Graph graph, ContextManager contextManager) throws RDFParseException, RDFHandlerException, IOException {
		load(reader, graph, contextManager, null);
	}
	
	public void load(InputStream input, Graph graph, ContextManager contextManager, NamespaceManager nsManager) throws RDFParseException, RDFHandlerException, IOException {
		InputStreamReader reader = new InputStreamReader(input);
		load(reader, graph, contextManager, nsManager);
	}
	
	public void load(InputStream input, Graph graph) throws RDFParseException, RDFHandlerException, IOException {
		load(input, graph, null, null);
	}
	
	public void load(Reader reader, Graph graph, ContextManager contextManager, NamespaceManager nsManager) throws RDFParseException, RDFHandlerException, IOException {

		JsonldParser parser = new JsonldParser(contextManager, nsManager);
		GraphLoadHandler handler = new GraphLoadHandler(graph);
		handler.setQuadContext(quadContext);
		
		parser.setRDFHandler(handler);
		parser.parse(reader, "");
	}

	public URI getQuadContext() {
		return quadContext;
	}

	public void setQuadContext(URI quadContext) {
		this.quadContext = quadContext;
	}
	
	

}
