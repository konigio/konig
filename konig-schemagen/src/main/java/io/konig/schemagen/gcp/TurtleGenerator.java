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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.VertexCopier;

public class TurtleGenerator {

	public void generateAll(URI resourceType, File baseDir, Graph graph, VelocityContext context) throws KonigException, IOException, RDFHandlerException {
	
		generateInstances(resourceType, baseDir, graph, context);
	}
	

	private void generateInstances(URI resourceType, File baseDir, Graph graph,VelocityContext context) throws KonigException, IOException, RDFHandlerException {
	
		List<Vertex> list = graph.v(resourceType).in(RDF.TYPE).toVertexList();
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
					
					
					StringWriter result = new StringWriter();
						
					Properties properties = new Properties();
					properties.setProperty("file.resource.loader.path", baseDir.getPath());
					VelocityEngine engine = new VelocityEngine(properties);
					Template template = engine.getTemplate(fileName, "UTF-8");
					template.merge(context, result );
					
					try (PrintWriter out = new PrintWriter(turtleFile)) {
					    out.println(result.toString());
					}
									
				} else {
					
					throw new KonigException(resourceType.getLocalName() + " must have a URI: " + v);
				}
			}
		}
	}
	

}
