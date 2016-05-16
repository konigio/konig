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


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.ldp.LdpException;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.LdpWriter;
import io.konig.ldp.MediaType;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceFile;

public class TurtleLdpWriter implements LdpWriter {

	@Override
	public void write(LdpResponse response) throws LdpException, IOException {
		
		ResourceFile resource = response.getResource();
		MediaType target = response.getTargetMediaType();
		String contentType = resource.getContentType();
		OutputStream out = response.getOutputStream();

		if (
			"text/turtle".equals(contentType) &&
			contentType.equals(target.getFullName()) &&
			resource.getEntityBody() != null
		) {
			out.write(resource.getEntityBody());
			
		} else if (resource.isRdfSource()) {
			
			Graph graph = resource.asRdfSource().createGraph();
			if (graph != null) {
				try {
					writeGraph(graph, out);
				} catch (RDFHandlerException e) {
					throw new LdpException(e);
				}
			} else {
				throw new LdpException("Graph is not defined");
			}
		}
		
		
	}


	private void writeGraph(Graph graph, OutputStream out) throws RDFHandlerException {
		
		TurtleWriter writer = new TurtleWriter(out);
		
		writer.startRDF();
		writeNamespaces(graph, writer);
		
		for (Statement s : graph) {
			writer.handleStatement(s);
		}
		
		writer.endRDF();
		
		
		
		
	}

	private void writeNamespaces(Graph graph, TurtleWriter writer) throws RDFHandlerException {

		NamespaceManager ns = graph.getNamespaceManager();
		if (ns != null) {
			List<Namespace> list = new ArrayList<>(ns.listNamespaces());
			Collections.sort(list, new Comparator<Namespace>() {

				@Override
				public int compare(Namespace a, Namespace b) {
					String x = a.getName();
					String y = b.getName();

					return x.compareTo(y);
				}
			});
			
			for (Namespace namespace : list) {
				writer.handleNamespace(namespace.getPrefix(), namespace.getName());
			}
		}
		
	}

	

}
