package io.konig.core.io;

/*
 * #%L
 * Konig Core
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
import java.text.MessageFormat;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.VANN;

public class SkosEmitter implements Emitter {
	
	private File outDir;

	public SkosEmitter(File outDir) {
		this.outDir = outDir;
	}

	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		
		Vertex schemeType = graph.getVertex(SKOS.CONCEPT_SCHEME);
		if (schemeType != null) {
			List<Vertex> schemeList = schemeType.asTraversal().in(RDF.TYPE).toVertexList();
			if (!schemeList.isEmpty()) {
				if (!outDir.exists()) {
					outDir.mkdirs();
				}
						
				for (Vertex scheme : schemeList) {
					emit(scheme);
				}
			}
		}

	}



	private void emit(Vertex scheme) throws KonigException {
		
		String prefix = scheme.stringValue(VANN.preferredNamespacePrefix);
		NamespaceManager nsManager = scheme.getGraph().getNamespaceManager();
		if (prefix == null) {
			if (nsManager != null) {
				Namespace ns = nsManager.findByName(scheme.getId().stringValue());
				if (ns != null) {
					prefix = ns.getPrefix();
				}
			}
		}
		if (prefix == null) {
			String msg = MessageFormat.format(
					"vann:preferredNamespacePrefix is not defined for SKOS scheme <{0}>", 
					scheme.getId().stringValue());
			
			throw new KonigException(msg);
		}


		Graph target = new MemoryGraph(nsManager);
		
		VertexCopier copier = new VertexCopier();
		
		copier.deepCopy(scheme, target);
		
		List<Vertex> conceptList = scheme.asTraversal().in(SKOS.IN_SCHEME).toVertexList();
		for (Vertex concept : conceptList) {
			copier.deepCopy(concept, target);
		}
		
		File schemeFile = new File(outDir, prefix + ".ttl");
		
		try {
			RdfUtil.prettyPrintTurtle(nsManager, target, schemeFile);
		} catch (RDFHandlerException | IOException e) {
			String msg = "Failed to write file: " + schemeFile.getAbsolutePath();
			throw new KonigException(msg);
		}
		
		
	}



}
