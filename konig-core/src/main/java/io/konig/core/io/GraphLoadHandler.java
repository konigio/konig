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


import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;

public class GraphLoadHandler extends RDFHandlerBase {
	
	private Graph graph;
	private URI quadContext;
	
	public GraphLoadHandler(Graph graph) {
		super();
		this.graph = graph;
	}

	@Override
	public void handleNamespace(String prefix, String name) throws RDFHandlerException {
		NamespaceManager ns = graph.getNamespaceManager();
		if (ns != null) {
			ns.add(prefix, name);
		}
	}
	
	
	@Override
	public void handleStatement(Statement statement) throws RDFHandlerException {
		Resource subject = statement.getSubject();
		URI predicate = statement.getPredicate();
		Value object = statement.getObject();
		Resource context = statement.getContext();
		
		if (context == null) {
			context = quadContext;
		}
		
		graph.edge(subject, predicate, object, context);
	}

	public URI getQuadContext() {
		return quadContext;
	}

	public void setQuadContext(URI quadContext) {
		this.quadContext = quadContext;
	}
}