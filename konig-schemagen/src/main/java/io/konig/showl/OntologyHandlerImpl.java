package io.konig.showl;

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

	@Override
	public void handleShapeOntologies(Graph shapeOntologies) throws ShowlException {

		try {

			File outFile = fileGetter.getFile(OntologyWriter.SHAPE_NAMESPACE_FILE);
			if (outFile.exists()) {
				dataModelManager.reportUpdate(reportWriter, outFile);
			} else {
				dataModelManager.reportCreate(reportWriter, outFile);
			}
			
			if (reportWriter != null) {

				MemoryGraph original = new MemoryGraph();
				Set<String> namespaceSet = extractor.shapeNamespaces(source);
				extractor.collectShapeOntologies(source, namespaceSet, original);
				
				
				Graph delta = maker.createChangeSet(original, shapeOntologies, null);
				delta.setNamespaceManager(nsManager);
				
				PlainTextChangeSetReportWriter worker = new PlainTextChangeSetReportWriter(nsManager);
				worker.write(delta, reportWriter);
				
			}
			
			if (writeFile) {
				RdfUtil.prettyPrintTurtle(nsManager, shapeOntologies, outFile);
			}
		} catch (IOException | RDFHandlerException e) {
			throw new ShowlException("Failed to handle shape ontologies", e);
		}
		

	}

}
