package io.konig.schemagen.gcp;

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
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.VertexCopier;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GoogleCloudSqlDatabase;
import io.konig.gcp.datasource.GoogleCloudSqlTableInfo;
import io.konig.gcp.io.GoogleCloudSqlJsonUtil;

public class CloudSqlRdfGenerator {

	public void generateAll(File baseDir, Graph graph) throws KonigException, IOException, RDFHandlerException {
	
		generateInstances(baseDir, graph);
	}
	

	private void generateInstances(File baseDir, Graph graph) throws KonigException, IOException, RDFHandlerException {
	
		List<Vertex> list = graph.v(GCP.GoogleCloudSqlInstance).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {

			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}

			for (Vertex v : list) {
				Resource id = v.getId();
				if (id instanceof URI) {
					URI uri = (URI) id;
					String fileName = uri.getLocalName() + ".ttl";
					File turtleFile = new File(baseDir, fileName);
					
					MemoryGraph buffer = new MemoryGraph(graph.getNamespaceManager());
					VertexCopier copier = new VertexCopier();
					copier.deepCopy(v, buffer);
					
					try (FileWriter writer = new FileWriter(turtleFile)) {
						RdfUtil.prettyPrintTurtle(buffer, writer);
					}
					
				} else {
					throw new KonigException("GoogleCloudSqlInstance must have a URI: " + v);
				}
			}
		}
	}
	

}
