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

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.showl.OntologyFileGetter;
import io.konig.showl.OntologyWriter;

public class OntologyEmitter implements Emitter {
	
	private File outDir;
	
	public OntologyEmitter(File outDir) {
		this.outDir = outDir;
	}

	@Override
	public void emit(Graph graph) throws KonigException, IOException {
		
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		OntologyWriter ontoWriter = new OntologyWriter(
				new OntologyFileGetter(outDir, graph.getNamespaceManager()));
		
			ontoWriter.writeOntologies(graph);
	}


}
