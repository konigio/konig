package io.konig.schemagen.io;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.Emitter;

public class NamedGraphEmitter implements Emitter {
	
	private File rootDir;
	

	public NamedGraphEmitter(File rootDir) {
		this.rootDir = rootDir;
	}

	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		Worker worker = new Worker();
		worker.emit(graph);
	}
	
	class Worker {
		private Map<URI, MemoryGraph> map = new HashMap<>();

		public void emit(Graph graph) {
			buildMap(graph);
			writeFiles();
			
		}

		private void writeFiles() {
			rootDir.mkdirs();
			
			for (Map.Entry<URI, MemoryGraph> e : map.entrySet()) {
				URI namespaceId = e.getKey();
				MemoryGraph g = e.getValue();
				NamespaceManager nsManager = g.getNamespaceManager();
				Namespace ns = nsManager.findByName(namespaceId.stringValue());
				if (ns == null) {
					throw new KonigException("Namespace not found: " + namespaceId);
				}
				
				String fileName = ns.getPrefix() + ".ttl";
				File file = new File(rootDir, fileName);
				try {
					RdfUtil.prettyPrintTurtle(nsManager, g, file);
				} catch (RDFHandlerException | IOException oops) {
					throw new KonigException(oops);
				}
				
			}
			
		}

		private void buildMap(Graph graph) {
			for (Edge edge : graph) {
				Resource context = edge.getContext();
				if (context instanceof URI) {
					URI namespace = RdfUtil.namespace(graph, context.stringValue());
					if (namespace != null) {
						
						MemoryGraph g = map.get(namespace);
						if (g == null) {
							g = new MemoryGraph(graph.getNamespaceManager());
							map.put(namespace, g);
						}
						g.edge(edge);
					}
				}
			}
			
		}
	}

}
