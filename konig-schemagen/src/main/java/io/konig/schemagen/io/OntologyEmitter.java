package io.konig.schemagen.io;

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
