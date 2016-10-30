package io.konig.showl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.delta.BNodeKeyFactory;
import io.konig.core.delta.ChangeSetFactory;
import io.konig.core.delta.ChangeSetProcessor;
import io.konig.core.delta.OwlRestrictionKeyFactory;
import io.konig.core.delta.PlainTextChangeSetReportWriter;
import io.konig.core.extract.ExtractException;
import io.konig.core.extract.OntologyExtractor;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;

/**
 * A job that merges two versions of an ontology to create a new version.
 * @author Greg McFall
 *
 */
public class ModelMergeJob {
	
	private static final String COPY = "COPY ";
	private static final String CREATE = "CREATE ";
	private static final String UPDATE = "UPDATE ";
	private static final String DELETE = "DELETE ";
	private static final String DELIMITER = "===============================================================================";
	
	private String outDirPath;

	private File oldDir;
	private File newDir;
	private File mergeDir;
	
	private String mergeDirPath;
	
	private boolean writeEnabled;
	private boolean reportEnabled;
	private FileGetter ontologyFileGetter;
	private Graph oldGraph;
	private Graph newGraph;
	private PrintWriter reportWriter;
	private NamespaceManager nsManager;
	private OntologyExtractor ontologyExtractor;
	private ChangeSetFactory maker;
	private ChangeSetProcessor processor;
	private BNodeKeyFactory keyExtractor;

	public ModelMergeJob(
		File oldDir,
		File newDir,
		File mergeDir,
		PrintWriter reportStream
	) throws ShowlException {
		this.oldDir = oldDir;
		this.newDir = newDir;
		this.mergeDir = mergeDir;
		this.nsManager = new MemoryNamespaceManager();
		this.reportWriter = reportStream;
		oldGraph = new MemoryGraph();
		newGraph = new MemoryGraph();
		writeEnabled = mergeDir != null;
		if (!writeEnabled) {
			this.mergeDir = newDir;
		}
		reportEnabled = reportWriter!=null;
		
	}
	
	
	
	public boolean isReportEnabled() {
		return reportEnabled;
	}



	public void setReportEnabled(boolean reportEnabled) {
		this.reportEnabled = reportEnabled;
	}



	public void run() throws ShowlException {
		computePaths();
		try {
			RdfUtil.loadTurtle(oldDir, oldGraph, nsManager);
			RdfUtil.loadTurtle(newDir, newGraph, nsManager);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new ShowlException(e);
		}
		
		
		ontologyFileGetter = new OntologyFileGetter(mergeDir, nsManager);
		ontologyExtractor = new OntologyExtractor();
		maker = new ChangeSetFactory();
		processor = new ChangeSetProcessor();
		keyExtractor = createKeyExtractor();
		
		Set<String> oldShapeNamespaces = ontologyExtractor.shapeNamespaces(oldGraph);
		Set<String> newShapeNamespaces = ontologyExtractor.shapeNamespaces(newGraph);
		
		Set<String> oldOntologyList = ontologyExtractor.collectOwlOntologies(oldGraph, oldShapeNamespaces);
		Set<String> newOntologyList = ontologyExtractor.collectOwlOntologies(newGraph, newShapeNamespaces);
		
		mergeOntologies(oldOntologyList, newOntologyList);
	}

	/**
	 * Emit the merged set of ontologies.
	 * @param oldOntologySet  The list of URI values (as strings) for ontologies contained in the old set.
	 * @param newOntologySet  The list of URI values (as strings) for ontologies contained in the new set.
	 * @throws ShowlException
	 */
	private void mergeOntologies(Set<String> oldOntologySet, Set<String> newOntologySet) throws ShowlException {
		
		
		handleOldOntologies(oldOntologySet, newOntologySet);
		handleNewOntologies(oldOntologySet, newOntologySet);
		
		Set<String> joint = intersection(oldOntologySet, newOntologySet);
		
		for (String uri : joint) {
			URI ontologyId = new URIImpl(uri);
			doMergeOntology(ontologyId);
		}
		
	}

	private void doMergeOntology(URI ontologyId) throws ShowlException {
		
		MemoryGraph oldOntology = new MemoryGraph();
		MemoryGraph newOntology = new MemoryGraph();
		try {
			Vertex oldVertex = oldGraph.getVertex(ontologyId);
			Vertex newVertex = newGraph.getVertex(ontologyId);
			ontologyExtractor.extract(oldVertex, oldOntology);
			ontologyExtractor.extract(newVertex, newOntology);
			
			maker.setPreserveNamedIndividuals(true);
			Graph merged = maker.createChangeSet(oldOntology, newOntology, keyExtractor);
			

			File file = ontologyFileGetter.getFile(ontologyId);
			if (reportEnabled) {
				if (!merged.isEmpty()) {
					String path = getPath(file);
					reportWriter.print(UPDATE);
					reportWriter.println(path);
					PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
					reporter.write(merged, reportWriter);
				}
			}
			if (writeEnabled) {

				MemoryGraph target = new MemoryGraph();
				processor.applyChanges(oldGraph, merged, target);
				RdfUtil.prettyPrintTurtle(nsManager, target, file);
			}
			
			
		} catch (ExtractException | IOException | RDFHandlerException e) {
			throw new ShowlException(e);
		}
		
	}

	private void handleNewOntologies(Set<String> oldOntologySet, Set<String> newOntologySet) throws ShowlException {
		List<String> newOnly = complement(newOntologySet, oldOntologySet);
		for (String value : newOnly) {
			
			URI uri = uri(value);
			File file = ontologyFileGetter.getFile(uri);
			String path = getPath(file);
			if (reportEnabled) {
				reportWriter.print(CREATE);
				reportWriter.println(path);
			}
			if (writeEnabled) {
				writeOntology(file, uri, newGraph);
			}
		}
		
	}

	private void handleOldOntologies(Set<String> oldOntologySet, Set<String> newOntologySet) throws ShowlException {

		List<String> oldOnly = complement(oldOntologySet, newOntologySet);
		for (String value : oldOnly) {
			
			URI uri = uri(value);
			File file = ontologyFileGetter.getFile(uri);
			String path = getPath(file);
			if (reportEnabled) {
				reportWriter.print(COPY);
				reportWriter.println(path);
			}
			if (writeEnabled) {
				writeOntology(file, uri, oldGraph);
			}
		}
		
		
	}

	/**
	 * Write the elements of an ontology to a file.
	 * @param file The file to which ontology elements will be written. 
	 * @param uri The URI of the ontology.
	 * @param graph The graph that contains the elements of the ontology.
	 * @throws ExtractException 
	 */
	private void writeOntology(File file, URI uri, Graph graph) throws ShowlException {
		try {
			Vertex ontology = graph.getVertex(uri);
			OntologyExtractor extractor = new OntologyExtractor();
			MemoryGraph target = new MemoryGraph();
			extractor.extract(ontology, target);
			
			RdfUtil.prettyPrintTurtle(nsManager, target, file);
			
		} catch (RDFHandlerException | IOException | ExtractException e) {
			throw new ShowlException(e);
		} finally {
			
		}
		
	}

	private String getPath(File file) {
		return file.getName();
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	/**
	 * Compute the set of values in 'a' and 'b'
	 */
	private Set<String> intersection(Set<String> a, Set<String> b) {
		Set<String> result = new HashSet<String>();
		for (String s : a) {
			if (b.contains(s)) {
				result.add(s);
			}
		}
		return result;
	}

	/**
	 * Compute the set of values in 'a' but not in 'b'.
	 */
	private List<String> complement(Set<String> a, Set<String> b) {
		List<String> result = new ArrayList<String>();
		for (String s : a) {
			if (!b.contains(s)) {
				result.add(s);
			}
		}
		Collections.sort(result);
		return result;
	}

	private void computePaths() {
		mergeDirPath = mergeDir.getAbsolutePath();
		
	}

	private BNodeKeyFactory createKeyExtractor() {
		
		return new OwlRestrictionKeyFactory();
	}
	
	public void reportCreate(PrintWriter out, File file) {
		out.println(DELIMITER);
		out.print(CREATE);
		printFile(out, file);
	}

	public void reportUpdate(PrintWriter out, File file) {
		out.println(DELIMITER);
		out.print(UPDATE);
		printFile(out, file);
	}
	

	public void reportDelete(PrintWriter out, File file) {
		out.println(DELIMITER);
		out.print(DELETE);
		printFile(out, file);
	}


	private void printFile(PrintWriter out, File file) {

		String filePath = file.getAbsolutePath().replace('\\', '/');
		if (filePath.startsWith(outDirPath)) {
			filePath = filePath.substring(outDirPath.length());
		}
		out.println(filePath);
		
	}
	
	
	

	public void handleOwlOntology(URI ontologyId, Graph ontologyGraph) throws ShowlException {
		
		File outFile = ontologyFileGetter.getFile(ontologyId);
		try {
			if (reportWriter != null) {
				printReport(outFile, ontologyId, ontologyGraph);
			}
			
			if (writeEnabled) {
				RdfUtil.prettyPrintTurtle(nsManager, ontologyGraph, outFile);
			}
			
		} catch (ExtractException | IOException | RDFHandlerException e) {
			throw new ShowlException("Failed to handle ontology: " + ontologyId, e);
		}

	}

	private String relativePath(String base, File file) {
		String path = file.getAbsolutePath();
		return path.substring(base.length());
	}
	
	
	private File oldFile(File mergeFile) {
		String path = relativePath(mergeDirPath, mergeFile);
		return new File(oldDir, path);
	}
	
	private void printReport(File mergeFile, URI ontologyId, Graph ontologyGraph) throws ExtractException, IOException {
		File oldFile = oldFile(mergeFile);
		
		Vertex v = oldGraph.getVertex(ontologyId);
		if (oldFile.exists()) {
			reportUpdate(reportWriter, mergeFile);
		} else {
			reportCreate(reportWriter, mergeFile);
		}
		if (v != null) {
			MemoryGraph original = new MemoryGraph();
			ontologyExtractor.extract(v, original);
			Graph delta = maker.createChangeSet(original, ontologyGraph, keyExtractor);
			PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
			reporter.write(delta, reportWriter);
		}
		
	}

	public void handleShapeOntologies(Graph shapeOntologies) throws ShowlException {

		try {

			File outFile = ontologyFileGetter.getFile(OntologyWriter.SHAPE_NAMESPACE_FILE);
			if (outFile.exists()) {
				reportUpdate(reportWriter, outFile);
			} else {
				reportCreate(reportWriter, outFile);
			}
			
			if (reportWriter != null) {

				MemoryGraph original = new MemoryGraph();
				Set<String> namespaceSet = ontologyExtractor.shapeNamespaces(oldGraph);
				ontologyExtractor.collectShapeOntologies(oldGraph, namespaceSet, original);
				
				
				Graph delta = maker.createChangeSet(original, shapeOntologies, null);
				delta.setNamespaceManager(nsManager);
				
				PlainTextChangeSetReportWriter worker = new PlainTextChangeSetReportWriter(nsManager);
				worker.write(delta, reportWriter);
				
			}
			
			if (writeEnabled) {
				RdfUtil.prettyPrintTurtle(nsManager, shapeOntologies, outFile);
			}
		} catch (IOException | RDFHandlerException e) {
			throw new ShowlException("Failed to handle shape ontologies", e);
		}
		

	}
	

}
