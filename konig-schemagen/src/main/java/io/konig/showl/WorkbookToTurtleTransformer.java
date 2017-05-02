package io.konig.showl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.VertexCopier;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.spreadsheet.IdMapper;
import io.konig.spreadsheet.SpreadsheetException;
import io.konig.spreadsheet.WorkbookLoader;

/**
 * A utility which loads OWL + SHACL statements from a workbook and publishes the
 * workbook contents as a collection of Turtle files.
 * @author Greg McFall
 *
 */
public class WorkbookToTurtleTransformer {

	private IdMapper datasetMapper;
	private WorkbookLoader workbookLoader;
	private NamespaceManager nsManager;
	
	public WorkbookToTurtleTransformer(IdMapper datasetMapper, NamespaceManager nsManager) {
		this.datasetMapper = datasetMapper;

		this.nsManager = nsManager;
		workbookLoader = new WorkbookLoader(nsManager);
	}
	
	public WorkbookLoader getWorkbookLoader() {
		return workbookLoader;
	}

	public void transform(File workbookFile, File owlOutDir, File shapesOutDir) throws IOException, SpreadsheetException, RDFHandlerException {
		
		if (workbookFile == null) {
			throw new SpreadsheetException("workbookFile must be defined");
		}
		if (!workbookFile.exists()) {
			throw new SpreadsheetException("File not found: " + workbookFile);
		}
		FileInputStream input = new FileInputStream(workbookFile);
		try {

			owlOutDir.mkdirs();
			if (shapesOutDir != null) {
				shapesOutDir.mkdirs();
			}
			Workbook workbook = new XSSFWorkbook(input);
			
			Graph graph = new MemoryGraph();
			graph.setNamespaceManager(nsManager);
			
			workbookLoader.setDatasetMapper(datasetMapper);
			workbookLoader.load(workbook, graph);
			
			nsManager = workbookLoader.getNamespaceManager();
			graph.setNamespaceManager(nsManager);
			
			OntologyWriter ontologyWriter = new OntologyWriter(new OntologyFileGetter(owlOutDir, nsManager));
			ontologyWriter.writeOntologies(graph);
			
			if (shapesOutDir != null) {
				writeShapes(shapesOutDir);
			}
			
			
		} finally {
			input.close();
		}
	}

	private void writeShapes(File shapesOutDir) throws RDFHandlerException, IOException {
		ShapeFileGetter fileGetter = new ShapeFileGetter(shapesOutDir, nsManager);
		ShapeManager shapeManager = workbookLoader.getShapeManager();
		VertexCopier copier = new VertexCopier();
		copier.excludeProperty(SH.shape, SH.predicate, SH.targetClass, SH.valueClass);
		copier.excludeClass(OWL.CLASS, OWL.DATATYPEPROPERTY, OWL.OBJECTPROPERTY, OWL.FUNCTIONALPROPERTY, RDF.PROPERTY);
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;

				Vertex shapeVertex = workbookLoader.getGraph().getVertex(shapeURI);
				Graph graph = new MemoryGraph();
				copier.deepCopy(shapeVertex, graph);
				File shapeFile = fileGetter.getFile(shapeURI);
				RdfUtil.prettyPrintTurtle(nsManager, graph, shapeFile);
			}
			
		}
		
	}
	
}
