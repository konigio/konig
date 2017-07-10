package io.konig.showl;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import java.io.PrintWriter;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.delta.BNodeKeyFactory;
import io.konig.core.delta.ChangeSetFactory;
import io.konig.core.delta.OwlRestrictionKeyFactory;
import io.konig.core.delta.PlainTextChangeSetReportWriter;
import io.konig.core.extract.ExtractException;
import io.konig.core.extract.OntologyExtractor;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.FileGetter;

public class OntologyHandlerImpl implements OntologyHandler {

	private FileGetter fileGetter;
	private Graph source;
	private PrintWriter reportWriter;
	private NamespaceManager nsManager;
	private boolean writeFile;
	private OntologyExtractor extractor;
	private ChangeSetFactory maker;
	private BNodeKeyFactory keyFactory;
	private ModelMergeJob dataModelManager;
	

	public OntologyHandlerImpl(
		ModelMergeJob dataModelManager,
		NamespaceManager nsManager,
		FileGetter fileGetter, 
		Graph source, 
		PrintWriter reportStream, 
		boolean writeFile
	) {
		this.dataModelManager = dataModelManager;
		this.nsManager = nsManager;
		this.fileGetter = fileGetter;
		this.source = source;
		this.reportWriter = reportStream;
		this.writeFile = writeFile;
		extractor = new OntologyExtractor();
		maker = new ChangeSetFactory();
		keyFactory = createKeyFactory();
	}



	private BNodeKeyFactory createKeyFactory() {
		
		return new OwlRestrictionKeyFactory();
	}



	@Override
	public void handleOwlOntology(URI ontologyId, Graph ontologyGraph) throws ShowlException {
		
		File outFile = fileGetter.getFile(ontologyId);
		try {
			if (reportWriter != null) {
				printReport(outFile, ontologyId, ontologyGraph);
			}
			
			if (writeFile) {
				RdfUtil.prettyPrintTurtle(nsManager, ontologyGraph, outFile);
			}
			
		} catch (ExtractException | IOException | RDFHandlerException e) {
			throw new ShowlException("Failed to handle ontology: " + ontologyId, e);
		}

	}

	private void printReport(File outFile, URI ontologyId, Graph ontologyGraph) throws ExtractException, IOException {
		
		Vertex v = source.getVertex(ontologyId);
		if (outFile.exists()) {
			dataModelManager.reportUpdate(reportWriter, outFile);
		} else {
			dataModelManager.reportUpdate(reportWriter, outFile);
		}
		if (v != null) {
			MemoryGraph original = new MemoryGraph();
			extractor.extract(v, original);
			Graph delta = maker.createChangeSet(original, ontologyGraph, keyFactory);
			PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
			reporter.write(delta, reportWriter);
		}
		
	}


}
